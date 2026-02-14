package com.minierp.auth.api.dto;

public record RefreshTokenResponse(
        String accessToken,
        long expiresInSeconds
) {}
