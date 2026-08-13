package com.cyberdeck.application.config;

import com.cyberdeck.application.port.out.ActionCatalogPort;
import com.cyberdeck.domain.rules.DefaultRuleEngine;
import com.cyberdeck.domain.rules.Randomizer;
import com.cyberdeck.domain.rules.RuleEngine;
import com.cyberdeck.domain.rules.ThreadLocalRandomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El motor de reglas y la fuente de azar son dominio puro y no llevan
 * anotaciones de Spring, asi que se declaran como beans desde aqui.
 */
@Configuration
public class RuleEngineConfig {

    @Bean
    public Randomizer randomizer() {
        return new ThreadLocalRandomizer();
    }

    @Bean
    public RuleEngine ruleEngine(ActionCatalogPort catalogPort, Randomizer randomizer) {
        return new DefaultRuleEngine(catalogPort.catalog(), randomizer);
    }
}
