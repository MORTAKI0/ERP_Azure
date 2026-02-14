package com.minierp.auth.api.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
