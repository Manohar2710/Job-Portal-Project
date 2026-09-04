package com.learning.job_portal_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient arbeitnowWebClient() {
        // Arbeitnow full page response exceeds webclient 256 limit 
        // increasing the limit to 10MB
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(config -> config.defaultCodecs()
                .maxInMemorySize(10 * 1024 * 1024))
                .build();
        
        return WebClient.builder()
            .baseUrl("https://www.arbeitnow.com")
            .defaultHeader("Accept", "application/json")
            .exchangeStrategies(strategies)
            .build();
    }
}
