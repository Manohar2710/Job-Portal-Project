package com.learning.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secretKey;
    private Long expiration;
    private Long refreshExpiration;
    private String issuer;
}
