package com.learning.security.service;

import com.learning.security.entity.RefreshToken;
import com.learning.security.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyExpiration(String token);
    void deleteByUser(User user);

}
