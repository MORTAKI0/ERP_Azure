package com.minierp.auth.api;

import com.minierp.auth.api.dto.LoginRequest;
import com.minierp.auth.api.dto.LoginResponse;
import com.minierp.auth.api.dto.RefreshTokenRequest;
import com.minierp.auth.api.dto.RefreshTokenResponse;
import com.minierp.auth.user.domain.UserEntity;
import com.minierp.auth.security.JwtService;
import com.minierp.auth.service.RefreshTokenService;
import com.minierp.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        UserEntity user = userService.verifyCredentials(req.email(), req.password());
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issueFor(user);

        String accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new LoginResponse(
                accessToken,
                jwtService.getAccessTtlSeconds(),
                refreshToken.rawToken(),
                jwtService.getRefreshTtlSeconds()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        UserEntity user = refreshTokenService.validateAndGetUser(req.refreshToken());
        String accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new RefreshTokenResponse(
                accessToken,
                jwtService.getAccessTtlSeconds()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest req) {
        refreshTokenService.revoke(req.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
