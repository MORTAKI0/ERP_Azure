package com.minierp.auth.service;

import com.minierp.auth.user.domain.UserEntity;
import com.minierp.auth.user.infra.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity verifyCredentials(String email, String rawPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        boolean ok = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        if (!ok) throw new BadCredentialsException("Invalid credentials");


        return user;
    }
}
