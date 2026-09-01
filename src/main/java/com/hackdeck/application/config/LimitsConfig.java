package com.hackdeck.application.config;

import com.hackdeck.application.limits.CreationQuota;
import com.hackdeck.application.limits.LimitsProperties;
import com.hackdeck.application.limits.RetentionPolicy;
import com.hackdeck.domain.model.GameId;
import com.hackdeck.domain.model.TournamentId;
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

    @Bean
    public CreationQuota<GameId> gameCreationQuota(LimitsProperties properties) {
        return new CreationQuota<>(properties.maxLiveGamesPerIp());
    }

    @Bean
    public CreationQuota<TournamentId> tournamentCreationQuota(LimitsProperties properties) {
        return new CreationQuota<>(properties.maxLiveTournamentsPerIp());
    }
}
