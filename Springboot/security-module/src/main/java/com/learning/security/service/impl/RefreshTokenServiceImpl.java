package com.learning.security.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.security.config.JwtProperties;
import com.learning.security.entity.RefreshToken;
import com.learning.security.entity.User;
import com.learning.security.exception.TokenRefreshException;
import com.learning.security.repository.RefreshTokenRepository;
import com.learning.security.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    // 256 bits of entropy — far exceeds UUID's 122 bits
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    /*
    * PUBLIC API
    */

    /**
     * 
     *
     *
     */
    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // delete any existing token for this user before creating new one
        // this ensures one valid refresh token exists per user at a time
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(generateSecureToken())
            .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()))
            .build();
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token create for user ID {}", user.getId());
        return savedRefreshToken;
    }

    // Looks up the token in the database and checks wheather it has expired.
    /**
    * <p><strong>Token rotation:</strong> if the token IS expired, it is immediately
     * deleted from the database before throwing, so it can never be replayed.
     * Callers that receive a valid (non-expired) token should follow up with
     * {@link #createRefreshToken(User)} to rotate to a fresh token.</p>
     *
     * @param token the raw opaque token string from the client request
     * @return the {@link RefreshToken} entity, guaranteed to be non-expired
     * @throws SecurityExceptionHandler if the token is not found or has expired
     */
    @Override
    public RefreshToken verifyExpiration(String token) {
         RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found — possible reuse after logout or rotation");
                    return new TokenRefreshException(token, "Token not found. Please log in again.");
                });

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            // Eagerly delete the expired token so it cannot be retried
            refreshTokenRepository.delete(refreshToken);
            log.warn("Expired refresh token deleted for userId: {}", refreshToken.getUser().getId());
            throw new TokenRefreshException(token, "Token has expired. Please log in again.");
        }

        return refreshToken;
    }

    /**
     * Deletes all refresh tokens belonging to the given user (logout / account cleanup).
     */
    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Refresh token deleted for userId: {}", user.getId());
    }

    /**
     * Generates a cryptographically random, URL-safe Base64 token.
     * Uses {@link SecureRandom} which is seeded by the OS entropy source.
     * The result is 44 characters long (32 bytes → Base64, no padding).
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        // URL-safe alphabet, no padding — safe to use as a query param or header value
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
