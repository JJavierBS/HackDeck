package com.hackdeck.application.config;

import com.hackdeck.application.limits.LimitsProperties;
import com.hackdeck.application.limits.RetentionPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LimitsProperties.class)
public class LimitsConfig {

    @Bean
    public RetentionPolicy retentionPolicy(LimitsProperties properties) {
        return properties.retentionPolicy();
    }
}
