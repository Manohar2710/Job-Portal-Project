package com.learning.security.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

@ExceptionHandler(TokenRefreshException.class)
public ResponseEntity<Map<String, Object>> handleTokenRefreshException(TokenRefreshException ex) {
    log.warn("TokenRefreshException: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
        "timestamp", Instant.now(),
        "message", ex.getMessage(),
        "error", "Unauthorized",
        "status", HttpStatus.UNAUTHORIZED.value()
    ));
}
}
