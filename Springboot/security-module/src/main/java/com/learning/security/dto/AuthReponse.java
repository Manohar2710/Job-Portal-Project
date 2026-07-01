package com.learning.security.dto;

import java.util.List;

public record AuthReponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UserInfo user
) {

    public record UserInfo(
        Long id,
        String firstname,
        String lastname,
        List<String> roles
    ) {
    }

}
