package com.minierp.auth.service;

import com.minierp.auth.config.JwtProperties;
import com.minierp.auth.api.error.InvalidRefreshTokenException;
import com.minierp.auth.refresh.domain.RefreshTokenEntity;
import com.minierp.auth.refresh.infra.RefreshTokenRepository;
import com.minierp.auth.user.domain.UserEntity;
import com.minierp.auth.user.infra.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public IssuedRefreshToken issueFor(UserEntity user) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plusDays(jwtProperties.getRefreshTtlDays());

        String rawToken = generateSecureToken();
        String hash = hashToken(rawToken);

        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setTokenHash(hash);
        token.setCreatedAt(now);
        token.setExpiresAt(expiresAt);
        token.setRevokedAt(null);
        refreshTokenRepository.save(token);

        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public UserEntity validateAndGetUser(String rawRefreshToken) {
        RefreshTokenEntity token = getActiveTokenOrThrow(rawRefreshToken);

        return userRepository.findById(token.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        RefreshTokenEntity token = getActiveTokenOrThrow(rawRefreshToken);
        token.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(token);
    }

    private RefreshTokenEntity getActiveTokenOrThrow(String rawRefreshToken) {
        RefreshTokenEntity token = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }
        return token;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record IssuedRefreshToken(String rawToken, OffsetDateTime expiresAt) {}
}
