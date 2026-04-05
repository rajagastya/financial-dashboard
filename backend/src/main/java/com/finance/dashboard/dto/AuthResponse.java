package com.finance.dashboard.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        UserResponse user
) {
}
