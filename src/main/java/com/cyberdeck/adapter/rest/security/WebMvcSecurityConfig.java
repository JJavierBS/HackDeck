package com.cyberdeck.adapter.rest.security;

import com.cyberdeck.adapter.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcSecurityConfig implements WebMvcConfigurer {

    private final ParticipantSessionArgumentResolver participantSessionArgumentResolver;
    private final TournamentSessionArgumentResolver tournamentSessionArgumentResolver;
    private final SecurityProperties properties;

    public WebMvcSecurityConfig(
            ParticipantSessionArgumentResolver participantSessionArgumentResolver,
            TournamentSessionArgumentResolver tournamentSessionArgumentResolver,
            SecurityProperties properties) {
        this.participantSessionArgumentResolver = participantSessionArgumentResolver;
        this.tournamentSessionArgumentResolver = tournamentSessionArgumentResolver;
        this.properties = properties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(participantSessionArgumentResolver);
        resolvers.add(tournamentSessionArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.corsAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
