package com.cyberrange.application.config;

import com.cyberrange.domain.rules.DefaultRuleEngine;
import com.cyberrange.domain.rules.RuleEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El motor de reglas es dominio puro y no lleva anotaciones de Spring, asi
 * que se declara como bean desde aqui.
 */
@Configuration
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine() {
        return new DefaultRuleEngine();
    }
}
