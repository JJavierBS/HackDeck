package com.cyberrange.application.config;

import com.cyberrange.application.port.out.ActionCatalogPort;
import com.cyberrange.domain.rules.DefaultRuleEngine;
import com.cyberrange.domain.rules.Randomizer;
import com.cyberrange.domain.rules.RuleEngine;
import com.cyberrange.domain.rules.ThreadLocalRandomizer;
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
