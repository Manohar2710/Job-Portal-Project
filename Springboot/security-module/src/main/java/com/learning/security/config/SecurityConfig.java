package com.learning.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.learning.security.filter.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize / @Secured on controller methods
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    // -----------------------------------------------------------------------
    // SecurityFilterChain — the core security policy
    // -----------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
        // 1. Disable CSRF — not needed for stateless JWT APIs
        .csrf(AbstractHttpConfigurer::disable)
        // 2. Apply CORS policy from CorsConfigurationSource (security.cors.* in application.yaml)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        // 3. Stateless session — Spring must never create an HttpSession
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 4. Route authorisation rules
        .authorizeHttpRequests(auth -> auth
            // Public endpoints — no token required
            .requestMatchers(
                "/api/auth/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                // Actuator health/liveness/readiness probes — must be reachable without a token
                // (Kubernetes readiness/liveness probes do not send Authorization headers).
                // The management port (8081) is not exposed via the public ingress, so this
                // only widens access on the internal management interface.
                "/actuator/health",
                "/actuator/health/liveness",
                "/actuator/health/readiness",
                "/actuator/info"
            )
            .permitAll()
            // All other actuator endpoints (metrics, prometheus) stay authenticated
            .requestMatchers("/actuator/**").authenticated()
            // Everything else requires a valid JWT
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex                         
            .authenticationEntryPoint(
                (request, response, authException) ->
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication required"
                    )
                )
        )
        // 5. Plug in our DaoAuthenticationProvider
        .authenticationProvider(authenticationProvider())
        // 6. Register JWT filter BEFORE the default username/password filter
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
        
    }
    // -----------------------------------------------------------------------
    // Supporting beans
    // -----------------------------------------------------------------------

    /**
     * Wires together UserDetailsService + PasswordEncoder so Spring Security
     * knows how to load and verify users during authentication.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager so the auth controller can call
     * authenticate() directly when processing login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt is the standard for password hashing — strength 10 is the default.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
