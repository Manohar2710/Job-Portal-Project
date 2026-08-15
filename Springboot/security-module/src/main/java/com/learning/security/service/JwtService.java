package com.learning.security.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    public String generateToken(UserDetails userDetails);
    public boolean isTokenValid(String token, UserDetails userDetails);
    public String extractUsername(String token);
    List<String> extractRoles(String token);
}
