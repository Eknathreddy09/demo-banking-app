package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class BankUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public BankUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("CUSTOMER")
                .build();
    }
}
