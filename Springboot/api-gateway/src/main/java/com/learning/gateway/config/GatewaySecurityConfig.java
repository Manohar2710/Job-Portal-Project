package com.learning.gateway.config;

import com.learning.security.config.JwtProperties;
import com.learning.security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;

/**
 * Gateway security configuration:
 *
 * 1. Provides a minimal JwtService bean (signature + expiry check only —
 *    no UserDetails DB lookup needed at the edge).
 *
 * 2. Disables Spring Security's default "block everything" reactive filter
 *    chain. Per-route JWT protection is handled by JwtAuthFilter instead;
 *    Spring Security must permit all at the framework level so that
 *    /api/auth/** (login, register) is never intercepted here.
 *
 * SecurityModuleAutoConfiguration is excluded in application.yaml because its
 * broad @ComponentScan("com.learning.security") would pull in
 * UserDetailsServiceImpl → UserRepository → JpaRepository (not on classpath).
 */
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class GatewaySecurityConfig {

    /**
     * Open security chain — let every request through at the Spring Security
     * layer. The JwtAuthFilter on each protected route handles auth instead.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

    @Bean
    public JwtService jwtService(JwtProperties props) {
        return new JwtService() {

            @Override
            public String extractUsername(String token) {
                return extractClaim(token, Claims::getSubject);
            }

            @Override
            public String generateToken(UserDetails userDetails) {
                throw new UnsupportedOperationException("Gateway does not generate tokens");
            }

            @Override
            public boolean isTokenValid(String token, UserDetails userDetails) {
                throw new UnsupportedOperationException("Gateway does not validate tokens against UserDetails");
            }

            private <T> T extractClaim(String token, java.util.function.Function<Claims, T> resolver) {
                return resolver.apply(
                    Jwts.parser()
                        .verifyWith(signingKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                );
            }

            private SecretKey signingKey() {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getSecretKey()));
            }
        };
    }
}
