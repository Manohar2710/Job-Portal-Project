package com.learning.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Builds the {@link CorsConfigurationSource} bean from the values bound by
 * {@link CorsProperties} ({@code security.cors.*} in application.yaml).
 *
 * <p>Keeping this in its own class avoids cluttering {@link SecurityConfig} and
 * lets the CORS policy be tested or replaced independently.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedMethods(corsProperties.getAllowedMethods());

        // "*" as a single list element is a valid sentinel for CorsConfiguration
        List<String> headers = corsProperties.getAllowedHeaders();
        if (headers.size() == 1 && "*".equals(headers.get(0))) {
            config.addAllowedHeader("*");
        } else {
            config.setAllowedHeaders(headers);
        }

        config.setAllowCredentials(corsProperties.isAllowCredentials());
        config.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to every path handled by this service
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
