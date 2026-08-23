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

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(generateSecureToken())
            .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()))
            .build();
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created for userId: {}", user.getId());
        return saved;
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.warn("Refresh token not found — possible reuse after logout or rotation");
                return new TokenRefreshException(token, "Token not found. Please log in again.");
            });

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("Expired refresh token deleted for userId: {}", refreshToken.getUser().getId());
            throw new TokenRefreshException(token, "Token has expired. Please log in again.");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Refresh token deleted for userId: {}", user.getId());
    }

    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
