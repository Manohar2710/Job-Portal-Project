package com.learning.security.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(UserDetails userDetails);
    /** Generates a token with an explicit userId claim so downstream services can resolve it. */
    String generateToken(UserDetails userDetails, Long userId);
    boolean isTokenValid(String token, UserDetails userDetails);
    String extractUsername(String token);
    List<String> extractRoles(String token);
    /** Returns the numeric user-id embedded in the token, or null if absent. */
    Long extractUserId(String token);
}
