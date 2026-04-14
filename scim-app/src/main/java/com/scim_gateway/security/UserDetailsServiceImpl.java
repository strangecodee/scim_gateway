package com.scim_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom UserDetailsService Implementation
 * Loads user details for authentication
 * Currently supports configured admin user from application.properties
 * Can be extended to load from database (MongoDB) in production
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Value("${auth.default.username:admin}")
    private String defaultUsername;
    
    @Value("${auth.default.password:admin123}")
    private String defaultPassword;
    
    private final PasswordEncoder passwordEncoder;
    
    public UserDetailsServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check if username matches the configured admin user
        if (!defaultUsername.equals(username)) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        // Create user with admin role
        // Password is encoded with BCrypt for Spring Security authentication
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        
        return User.builder()
            .username(defaultUsername)
            .password(passwordEncoder.encode(defaultPassword))
            .authorities(authorities)
            .build();
    }
}
