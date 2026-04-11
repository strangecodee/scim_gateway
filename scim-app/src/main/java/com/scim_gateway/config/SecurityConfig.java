package com.scim_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable CSRF for API usage
        http.csrf(csrf -> csrf.disable());
        
        // Allow all requests (for development/testing)
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/scim/v2/**").permitAll()
            .anyRequest().permitAll()
        );
        
        // Disable form login and HTTP basic
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        
        return http.build();
    }
}
