package com.cyberrange.adapter.catalog;

import com.cyberrange.application.port.out.ActionCatalogPort;
import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.catalog.ActionCatalog;
import com.cyberrange.domain.catalog.CardDuration;
import com.cyberrange.domain.catalog.CardEffect;
import com.cyberrange.domain.catalog.CardType;
import com.cyberrange.domain.catalog.DefenseCategory;
import com.cyberrange.domain.catalog.KillChainPhase;
import com.cyberrange.domain.catalog.NoiseLevel;
import com.cyberrange.domain.model.CiaPillar;
import com.cyberrange.domain.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Carga el catalogo de un fichero YAML y lo valida al arrancar. Si algo
 * esta mal el backend no llega a levantar: es preferible a descubrir a
 * mitad de una clase que una carta apunta a un counter que no existe.
 *
 * Por defecto usa el catalogo empaquetado; con cyberrange.catalog.path el
 * instructor puede apuntar al suyo sin recompilar.
 */
@Component
public class YamlActionCatalogAdapter implements ActionCatalogPort {

    private static final Logger log = LoggerFactory.getLogger(YamlActionCatalogAdapter.class);

    private static final String DEFAULT_RESOURCE = "/catalog/cartas.yaml";

    private final ActionCatalog catalog;

    public YamlActionCatalogAdapter(@Value("${cyberrange.catalog.path:}") String externalPath) {
        this.catalog = load(externalPath);
        log.info("Catalogo cargado con {} cartas desde {}",
                catalog.size(),
                externalPath == null || externalPath.isBlank() ? "el catalogo empaquetado" : externalPath);
    }

    @Override
    public ActionCatalog catalog() {
        return catalog;
    }

    private ActionCatalog load(String externalPath) {
        Map<String, Object> root = readYaml(externalPath);
        List<?> rawCards = asList(root.get("cards"), "cards");
        if (rawCards.isEmpty()) {
            throw new InvalidCatalogException("El catalogo no tiene ninguna carta");
        }

        List<ActionCard> cards = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Object rawCard : rawCards) {
            ActionCard card = toCard(asMap(rawCard, "card"));
            if (!ids.add(card.id())) {
                throw new InvalidCatalogException("Hay dos cartas con el id '" + card.id() + "'");
            }
            cards.add(card);
        }
        validateReferences(cards, ids);
        return new ActionCatalog(cards);
    }

    private Map<String, Object> readYaml(String externalPath) {
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        if (externalPath != null && !externalPath.isBlank()) {
            Path path = Path.of(externalPath);
            if (!Files.isReadable(path)) {
                throw new InvalidCatalogException("No se puede leer el catalogo indicado en "
                        + "cyberrange.catalog.path: " + path.toAbsolutePath());
            }
            try (InputStream input = Files.newInputStream(path)) {
                return asMap(yaml.load(input), "catalogo");
            } catch (IOException e) {
                throw new InvalidCatalogException("Fallo al leer el catalogo " + path.toAbsolutePath(), e);
            }
        }
        try (InputStream input = getClass().getResourceAsStream(DEFAULT_RESOURCE)) {
            if (input == null) {
                throw new InvalidCatalogException("Falta el catalogo empaquetado en " + DEFAULT_RESOURCE);
            }
            return asMap(yaml.load(input), "catalogo");
        } catch (IOException e) {
            throw new InvalidCatalogException("Fallo al leer el catalogo empaquetado", e);
        }
    }

    private ActionCard toCard(Map<String, Object> raw) {
        String id = requireString(raw, "id", "una carta");
        String where = "la carta '" + id + "'";

        CardType type = requireEnum(CardType.class, requireString(raw, "type", where), "type", where);
        Role side = optionalEnum(Role.class, optionalString(raw, "side"), "side", where);
        KillChainPhase phase = optionalEnum(KillChainPhase.class, optionalString(raw, "phase"), "phase", where);
        DefenseCategory category =
                optionalEnum(DefenseCategory.class, optionalString(raw, "category"), "category", where);
        CardDuration duration =
                requireEnum(CardDuration.class, requireString(raw, "duration", where), "duration", where);

        int cost = intValue(raw, "cost", 0, where);
        double successRate = doubleValue(raw, "successRate", 1.0, where);
        int rounds = intValue(raw, "rounds", 0, where);

        validateShape(where, type, side, phase, category, duration, cost, successRate, rounds);

        return new ActionCard(
                id,
                type,
                side,
                phase,
                category,
                requireTranslations(raw, "name", where),
                translations(raw, "description"),
                cost,
                requireEnum(NoiseLevel.class, requireString(raw, "noise", where), "noise", where),
                successRate,
                duration,
                rounds,
                impacts(raw, where),
                intValue(raw, "mitigation", 0, where),
                intValue(raw, "detection", 0, where),
                strings(raw, "counters"),
                strings(raw, "requires"),
                phases(raw, where),
                bonuses(raw, where),
                effects(raw, where));
    }

    private static void validateShape(
            String where,
            CardType type,
            Role side,
            KillChainPhase phase,
            DefenseCategory category,
            CardDuration duration,
            int cost,
            double successRate,
            int rounds) {
        if (type == CardType.TWIST && side != null) {
            throw new InvalidCatalogException("Las cartas TWIST no llevan side, y " + where + " tiene uno");
        }
        if (type != CardType.TWIST && side == null) {
            throw new InvalidCatalogException("Falta side (ATTACKER o DEFENDER) en " + where);
        }
        if (type == CardType.ACTION && side == Role.ATTACKER && phase == null) {
            throw new InvalidCatalogException("Falta phase en " + where + ": toda accion ofensiva vive en una fase");
        }
        if (type == CardType.ACTION && side == Role.DEFENDER && category == null) {
            throw new InvalidCatalogException("Falta category en " + where);
        }
        if (side == Role.DEFENDER && phase != null) {
            throw new InvalidCatalogException("phase es solo del atacante, y lo lleva " + where);
        }
        if (duration == CardDuration.ROUNDS && rounds < 1) {
            throw new InvalidCatalogException("Con duration ROUNDS hay que decir cuantas en " + where);
        }
        if (cost < 0) {
            throw new InvalidCatalogException("El coste no puede ser negativo en " + where);
        }
        if (successRate < 0 || successRate > 1) {
            throw new InvalidCatalogException("successRate va de 0 a 1 en " + where);
        }
    }

    /**
     * Un counter o un requires que apunte a una carta inexistente es un
     * fallo silencioso en juego: la defensa simplemente no funcionaria.
     */
    private static void validateReferences(List<ActionCard> cards, Set<String> ids) {
        for (ActionCard card : cards) {
            String where = "la carta '" + card.id() + "'";
            for (String reference : card.counters()) {
                requireKnown(reference, ids, "counters", where);
            }
            for (String reference : card.requires()) {
                requireKnown(reference, ids, "requires", where);
            }
            for (String reference : card.bonus().keySet()) {
                requireKnown(reference, ids, "bonus", where);
            }
        }
    }

    private static void requireKnown(String reference, Set<String> ids, String field, String where) {
        if (!ids.contains(reference)) {
            throw new InvalidCatalogException(
                    "En " + where + ", el campo " + field + " apunta a '" + reference + "', que no existe");
        }
    }

    private Map<CiaPillar, Integer> impacts(Map<String, Object> raw, String where) {
        Map<CiaPillar, Integer> impacts = new EnumMap<>(CiaPillar.class);
        for (Map.Entry<String, Object> entry : asMap(raw.getOrDefault("impact", Map.of()), "impact").entrySet()) {
            CiaPillar pillar = requireEnum(CiaPillar.class, entry.getKey(), "impact", where);
            impacts.put(pillar, ((Number) entry.getValue()).intValue());
        }
        return impacts;
    }

    private Set<KillChainPhase> phases(Map<String, Object> raw, String where) {
        Set<KillChainPhase> phases = new HashSet<>();
        for (String value : strings(raw, "unlocks")) {
            phases.add(requireEnum(KillChainPhase.class, value, "unlocks", where));
        }
        return phases;
    }

    private Set<CardEffect> effects(Map<String, Object> raw, String where) {
        Set<CardEffect> effects = new HashSet<>();
        for (String value : strings(raw, "effects")) {
            effects.add(requireEnum(CardEffect.class, value, "effects", where));
        }
        return effects;
    }

    private Map<String, Double> bonuses(Map<String, Object> raw, String where) {
        Map<String, Double> bonuses = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : asMap(raw.getOrDefault("bonus", Map.of()), "bonus").entrySet()) {
            if (!(entry.getValue() instanceof Number number)) {
                throw new InvalidCatalogException("El bonus de " + where + " debe ser un numero");
            }
            bonuses.put(entry.getKey(), number.doubleValue());
        }
        return bonuses;
    }

    private static Map<String, String> requireTranslations(Map<String, Object> raw, String field, String where) {
        Map<String, String> translations = translations(raw, field);
        if (translations.isEmpty()) {
            throw new InvalidCatalogException("Falta " + field + " en " + where);
        }
        return translations;
    }

    private static Map<String, String> translations(Map<String, Object> raw, String field) {
        Map<String, String> translations = new LinkedHashMap<>();
        asMap(raw.getOrDefault(field, Map.of()), field)
                .forEach((language, text) -> translations.put(language, String.valueOf(text)));
        return translations;
    }

    private static List<String> strings(Map<String, Object> raw, String field) {
        List<String> values = new ArrayList<>();
        for (Object value : asList(raw.getOrDefault(field, List.of()), field)) {
            values.add(String.valueOf(value));
        }
        return values;
    }

    private static String requireString(Map<String, Object> raw, String field, String where) {
        String value = optionalString(raw, field);
        if (value == null) {
            throw new InvalidCatalogException("Falta " + field + " en " + where);
        }
        return value;
    }

    private static String optionalString(Map<String, Object> raw, String field) {
        Object value = raw.get(field);
        return value == null ? null : String.valueOf(value).strip();
    }

    private static int intValue(Map<String, Object> raw, String field, int fallback, String where) {
        Object value = raw.get(field);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Number number)) {
            throw new InvalidCatalogException("El campo " + field + " de " + where + " debe ser un numero entero");
        }
        return number.intValue();
    }

    private static double doubleValue(Map<String, Object> raw, String field, double fallback, String where) {
        Object value = raw.get(field);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof Number number)) {
            throw new InvalidCatalogException("El campo " + field + " de " + where + " debe ser un numero");
        }
        return number.doubleValue();
    }

    private static <E extends Enum<E>> E requireEnum(Class<E> type, String value, String field, String where) {
        E parsed = optionalEnum(type, value, field, where);
        if (parsed == null) {
            throw new InvalidCatalogException("Falta " + field + " en " + where);
        }
        return parsed;
    }

    private static <E extends Enum<E>> E optionalEnum(Class<E> type, String value, String field, String where) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCatalogException("Valor no valido en " + field + " de " + where + ": '" + value
                    + "'. Admitidos: " + String.join(", ", namesOf(type)));
        }
    }

    private static <E extends Enum<E>> List<String> namesOf(Class<E> type) {
        List<String> names = new ArrayList<>();
        for (E constant : type.getEnumConstants()) {
            names.add(constant.name());
        }
        return names;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value, String field) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new InvalidCatalogException("Se esperaba una lista de campos en " + field);
        }
        return (Map<String, Object>) map;
    }

    private static List<?> asList(Object value, String field) {
        if (!(value instanceof List<?> list)) {
            throw new InvalidCatalogException("Se esperaba una lista en " + field);
        }
        return list;
    }
}
