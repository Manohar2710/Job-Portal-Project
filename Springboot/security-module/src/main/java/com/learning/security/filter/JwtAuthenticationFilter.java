package com.learning.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.learning.security.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless JWT authentication filter — shared by every microservice that imports
 * security-module.
 *
 * Validates the Bearer token cryptographically using JwtService (HMAC-SHA256).
 * Builds the SecurityContext Authentication entirely from token claims:
 *   - subject  → username (email)
 *   - "roles"  → granted authorities
 *
 * No database is consulted — this filter is safe to use in any service
 * regardless of whether it has a UserRepository.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("No Bearer token on request [{} {}] — skipping JWT filter",
                    request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Build authorities from the "roles" claim — no DB call
                List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(jwt)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // Minimal UserDetails from claims only — password unused for JWT validation
                UserDetails userDetails = User.withUsername(username)
                        .password("")
                        .authorities(authorities)
                        .build();

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, authorities);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT validated — user: '{}', URI: [{} {}]",
                            username, request.getMethod(), request.getRequestURI());
                }
            }
        } catch (JwtException ex) {
            log.warn("Invalid JWT token on [{} {}]: {}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
