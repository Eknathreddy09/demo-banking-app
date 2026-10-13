package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + principal.getName()));
    }
}
