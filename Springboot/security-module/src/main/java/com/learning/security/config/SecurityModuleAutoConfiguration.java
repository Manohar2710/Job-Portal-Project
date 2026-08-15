package com.learning.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import com.learning.security.filter.JwtAuthenticationFilter;
import com.learning.security.service.impl.JwtServiceImpl;

/**
 * Auto-configuration entry point for the security-module shared library.
 *
 * Registers exactly the beans every microservice needs for stateless JWT validation:
 *   - JwtServiceImpl      — token generation, validation, claim extraction
 *   - JwtAuthenticationFilter — servlet filter that validates Bearer tokens
 *   - SecurityConfig      — stateless HttpSecurity filter chain + CORS
 *   - CorsConfig          — builds CorsConfigurationSource from application.yaml
 *   - JwtProperties       — binds security.jwt.* config properties
 *   - CorsProperties      — binds security.cors.* config properties
 *
 * Uses @Import (not @ComponentScan) so that only these six classes are registered.
 * No auth controllers, no DB-backed services, no UserDetailsService.
 * Any new microservice that adds security-module as a dependency gets exactly these
 * beans — no exclude filters, no workarounds required.
 */
@AutoConfiguration
@Import({
    JwtServiceImpl.class,
    JwtAuthenticationFilter.class,
    SecurityConfig.class,
    CorsConfig.class,
    JwtProperties.class,
    CorsProperties.class
})
public class SecurityModuleAutoConfiguration {
}
