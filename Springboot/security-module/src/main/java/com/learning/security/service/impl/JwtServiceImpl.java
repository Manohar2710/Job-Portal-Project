package com.learning.security.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.learning.security.config.JwtProperties;
import com.learning.security.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtProperties jwtProperties;


    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Generate a JWT for the given UserDetails.
     * Roles are embedded as a custom "roles" claim.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        extraClaims.put("roles", roles);
        return buildToken(extraClaims, userDetails, jwtProperties.getExpiration());
    }

    /** Generates a token with an explicit userId claim embedded for downstream services. */
    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        extraClaims.put("roles", roles);
        extraClaims.put("userId", userId);
        return buildToken(extraClaims, userDetails, jwtProperties.getExpiration());
    }
    
    /**
     * Validate the token against the supplied UserDetails.
     * Returns true only when the username matches AND the token is not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }
    /** Extract the subject (username) from the token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * function to extract roles from token for authorisation without hitting DB
     * 
     * */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims ->
            (List<String>) claims.getOrDefault("roles", List.of()));
    }

    /**
     * Extracts the numeric userId embedded as the "userId" claim.
     * Returns null if the claim is absent (tokens issued before this change).
     */
    @Override
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object raw = claims.get("userId");
            if (raw == null) return null;
            if (raw instanceof Long l) return l;
            if (raw instanceof Integer i) return i.longValue();
            if (raw instanceof Number n) return n.longValue();
            return null;
        });
    }
}
