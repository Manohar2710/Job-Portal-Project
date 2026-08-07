package com.learning.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.learning.security.service.JwtService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Gateway-level JWT validation filter.
 *
 * Strategy: call {@code extractUsername()} which internally verifies signature
 * and expiry via the JJWT library. A blank / null result or any exception
 * means the token is invalid — no UserDetails lookup needed at the edge.
 *
 * On success, the verified subject (user-id) is forwarded as {@code X-User-Id}
 * so downstream services can trust the caller without re-validating.
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or malformed Authorization header for {}", request.getURI());
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);
            try {
                // extractUsername throws if signature is invalid or token has expired
                String userId = jwtService.extractUsername(token);
                if (userId == null || userId.isBlank()) {
                    log.warn("JWT subject is empty for {}", request.getURI());
                    return unauthorized(exchange);
                }

                // Forward verified identity to downstream services as a trusted header
                ServerHttpRequest mutated = request.mutate()
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());

            } catch (Exception e) {
                log.warn("JWT validation error for {}: {}", request.getURI(), e.getMessage());
                return unauthorized(exchange);
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public static class Config {
        // No additional configuration required
    }
}
