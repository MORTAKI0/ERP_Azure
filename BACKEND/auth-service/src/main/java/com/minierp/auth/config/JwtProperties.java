package com.minierp.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessTtlMin,
        long refreshTtlDays
)
{
    public long getAccessTtlMin() {
        return accessTtlMin;
    }

    public long getRefreshTtlDays() {
        return refreshTtlDays;
    }
}
