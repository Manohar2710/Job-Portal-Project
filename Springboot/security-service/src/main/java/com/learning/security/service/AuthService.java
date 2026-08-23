package com.learning.security.service;

import com.learning.security.dto.AuthReponse;
import com.learning.security.dto.LoginRequest;
import com.learning.security.dto.LogoutRequest;
import com.learning.security.dto.RefreshTokenRequest;
import com.learning.security.dto.RegisterRequest;

public interface AuthService {
    AuthReponse register(RegisterRequest registerRequest);
    AuthReponse login(LoginRequest loginRequest);
    AuthReponse refresh(RefreshTokenRequest request);
    void logout(LogoutRequest request);
}
