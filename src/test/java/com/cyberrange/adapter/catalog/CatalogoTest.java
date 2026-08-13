package com.cyberrange.adapter.catalog;

import com.cyberrange.domain.catalog.ActionCard;
import com.cyberrange.domain.catalog.ActionCatalog;
import com.cyberrange.domain.catalog.CardDuration;
import com.cyberrange.domain.catalog.CardType;
import com.cyberrange.domain.catalog.NoiseLevel;
import com.cyberrange.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogoTest {

    private ActionCatalog catalogoEmpaquetado() {
        return new YamlActionCatalogAdapter("").catalog();
    }

    @Test
    void el_catalogo_que_se_distribuye_carga_y_tiene_cartas_de_los_dos_bandos() {
        ActionCatalog catalogo = catalogoEmpaquetado();

        assertThat(catalogo.playableBy(Role.ATTACKER)).isNotEmpty();
        assertThat(catalogo.playableBy(Role.DEFENDER)).isNotEmpty();
        assertThat(catalogo.twists()).isNotEmpty();
    }

    @Test
    void toda_carta_tiene_nombre_y_descripcion_en_los_dos_idiomas() {
        assertThat(catalogoEmpaquetado().all()).allSatisfy(carta -> {
            assertThat(carta.name()).containsKeys("es", "en");
            assertThat(carta.description()).containsKeys("es", "en");
        });
    }

    @Test
    void las_cartas_del_instructor_no_llevan_bando_y_las_demas_si() {
        assertThat(catalogoEmpaquetado().all()).allSatisfy(carta -> {
            if (carta.type() == CardType.TWIST) {
                assertThat(carta.side()).isNull();
            } else {
                assertThat(carta.side()).isNotNull();
            }
        });
    }

    @Test
    void una_carta_que_dura_rondas_dice_cuantas() {
        assertThat(catalogoEmpaquetado().all())
                .filteredOn(carta -> carta.duration() == CardDuration.ROUNDS)
                .isNotEmpty()
                .allSatisfy(carta -> assertThat(carta.rounds()).isPositive());
    }

    /**
     * Lo que pasa dentro de la red del defensor tiene que poder frenarse. Lo
     * que no hace ruido ocurre fuera (el OSINT es leer redes sociales) y no
     * hay defensa tecnica que valga.
     */
    @Test
    void todo_ataque_que_toca_la_red_del_defensor_tiene_quien_lo_contrarreste() {
        ActionCatalog catalogo = catalogoEmpaquetado();

        for (ActionCard ataque : catalogo.playableBy(Role.ATTACKER)) {
            if (ataque.type() != CardType.ACTION || ataque.noise() == NoiseLevel.NONE) {
                continue;
            }
            boolean alguienLoPara = catalogo.all().stream()
                    .anyMatch(otra -> otra.counters().containsKey(ataque.id()));
            assertThat(alguienLoPara)
                    .describedAs("la carta '%s' no la contrarresta nada", ataque.id())
                    .isTrue();
        }
    }

    @Test
    void un_counter_que_apunta_a_una_carta_inexistente_impide_arrancar(@TempDir Path carpeta) throws IOException {
        Path fichero = carpeta.resolve("roto.yaml");
        Files.writeString(fichero, """
                version: 1
                cards:
                  - id: phishing
                    type: ACTION
                    side: DEFENDER
                    category: HYGIENE
                    name: { es: "Phishing", en: "Phishing" }
                    cost: 10
                    noise: MEDIUM
                    duration: INSTANT
                    counters:
                      no-existe: 0.5
                """);

        assertThatThrownBy(() -> new YamlActionCatalogAdapter(fichero.toString()))
                .isInstanceOf(InvalidCatalogException.class)
                .hasMessageContaining("no-existe");
    }

    @Test
    void un_valor_mal_escrito_dice_que_carta_y_que_campo_fallan(@TempDir Path carpeta) throws IOException {
        Path fichero = carpeta.resolve("ruido.yaml");
        Files.writeString(fichero, """
                version: 1
                cards:
                  - id: ddos
                    type: ACTION
                    side: ATTACKER
                    phase: IMPACT
                    name: { es: "DDoS", en: "DDoS" }
                    cost: 14
                    noise: RUIDOSISIMO
                    duration: INSTANT
                """);

        assertThatThrownBy(() -> new YamlActionCatalogAdapter(fichero.toString()))
                .isInstanceOf(InvalidCatalogException.class)
                .hasMessageContaining("ddos")
                .hasMessageContaining("noise");
    }
}
