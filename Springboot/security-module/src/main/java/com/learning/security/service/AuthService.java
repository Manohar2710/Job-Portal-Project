package com.learning.security.service;

import com.learning.security.dto.AuthReponse;
import com.learning.security.dto.LoginRequest;
import com.learning.security.dto.LogoutRequest;
import com.learning.security.dto.RefreshTokenRequest;
import com.learning.security.dto.RegisterRequest;

public interface AuthService {
    AuthReponse register(RegisterRequest registerRequest);
    AuthReponse login(LoginRequest loginRequest);

    /**
     * Validates the supplied refresh token, rotates it (delete old → issue new),
     * and returns a fresh access token + refresh token pair.
     *
     * @throws com.learning.security.exception.TokenRefreshException if the token
     *         is not found in the database or has expired (→ HTTP 401)
     */
    AuthReponse refresh(RefreshTokenRequest request);

    /**
     * Invalidates the refresh token server-side so it can never be reused.
     * The client must discard its stored tokens after calling this endpoint.
     *
     * @throws com.learning.security.exception.TokenRefreshException if the token
     *         is not found (→ HTTP 401)
     */
    void logout(LogoutRequest request);
}
