package com.minierp.auth.user.infra;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserRepoSmokeTest implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserRepoSmokeTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        userRepository.findByEmail("admin@minierp.com")
                .ifPresentOrElse(
                        u -> System.out.println("[SMOKE] Found user: " + u.getEmail() + " roles=" + u.getRoles()),
                        () -> System.out.println("[SMOKE] admin@minierp.com not found")
                );
    }
}
