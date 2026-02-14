package com.minierp.auth.security;

import com.minierp.auth.config.JwtProperties;
import com.minierp.auth.user.domain.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;

        // If your JWT_SECRET is plain text, this works (must be long enough).
        // If you used base64, tell me and I’ll adjust to decode Base64.
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessTtlMin() * 60);

        List<String> roles = user.getRolesList(); // we’ll implement this helper in UserEntity

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(user.getEmail()) // subject = email (simple)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("roles", roles)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public ParsedToken validateAndParse(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(props.issuer())
                    .build()
                    .parseSignedClaims(token);

            Claims c = jws.getPayload();

            String subject = c.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) c.get("roles", List.class);

            return new ParsedToken(subject, roles, c.getExpiration().toInstant());
        } catch (JwtException e) {
            // includes expired, invalid signature, malformed...
            throw new RuntimeException("Invalid JWT", e);
        }
    }
    public long getAccessTtlSeconds() {
        return props.getAccessTtlMin() * 60L;
    }

    public long getRefreshTtlSeconds() {
        return props.getRefreshTtlDays() * 24L * 60L * 60L;
    }


    public record ParsedToken(String subject, List<String> roles, Instant expiresAt) {}
}
