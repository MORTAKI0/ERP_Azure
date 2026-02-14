package com.minierp.auth;

import com.minierp.auth.user.infra.UserRepository;
import com.minierp.auth.security.JwtService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SmokeJwtRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SmokeJwtRunner(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void run(String... args) {
        var user = userRepository.findByEmail("admin@minierp.com").orElseThrow();

        String token = jwtService.generateAccessToken(user);
        var parsed = jwtService.validateAndParse(token);

        System.out.println("[SMOKE] JWT subject=" + parsed.subject()
                + " roles=" + parsed.roles()
                + " exp=" + parsed.expiresAt());
    }
}
