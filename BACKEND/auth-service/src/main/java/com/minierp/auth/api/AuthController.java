package com.minierp.auth.api;

import com.minierp.auth.api.dto.LoginRequest;
import com.minierp.auth.api.dto.LoginResponse;
import com.minierp.auth.user.domain.UserEntity;
import com.minierp.auth.security.JwtService;
import com.minierp.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        UserEntity user = userService.verifyCredentials(req.email(), req.password());

        String token = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new LoginResponse(
                token,
                "Bearer",
                jwtService.getAccessTtlSeconds() // add this helper if you don't have it yet
        ));
    }
}
