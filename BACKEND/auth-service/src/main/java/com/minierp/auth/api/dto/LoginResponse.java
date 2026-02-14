package com.minierp.auth.api.dto;

public record LoginResponse(
        String accessToken,
        long expiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
) {}
