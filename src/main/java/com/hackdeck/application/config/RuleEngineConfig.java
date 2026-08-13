package com.hackdeck.application.config;

import com.hackdeck.application.port.out.ActionCatalogPort;
import com.hackdeck.domain.rules.DefaultRuleEngine;
import com.hackdeck.domain.rules.Randomizer;
import com.hackdeck.domain.rules.RuleEngine;
import com.hackdeck.domain.rules.ThreadLocalRandomizer;
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
