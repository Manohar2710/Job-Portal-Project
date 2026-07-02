package com.learning.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Read the Authorization header
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 2. Skip filter if header is absent or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 3. Strip "Bearer " prefix to get the raw token
        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 4. Extract username from token
            final String username = jwtService.extractUsername(jwt);
            // 5. Only authenticate if username is present and no auth is set yet
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. Load full UserDetails from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // 7. Validate the token against the loaded user
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 8. Build an authenticated token with granted authorities
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                        // credentials — null for JWT (stateless)
                                    userDetails.getAuthorities()
                            );
                    // 9. Attach request details (IP, session) to the authentication
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 10. Store in SecurityContext so downstream filters/controllers see it
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException ex) {
           // Invalid / expired / tampered token — clear context and let 401 propagate
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            return;
        }
        // 11. Continue the filter chain
        filterChain.doFilter(request, response);
    }

}
