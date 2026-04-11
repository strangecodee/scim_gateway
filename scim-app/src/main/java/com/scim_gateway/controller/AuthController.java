package com.scim_gateway.controller;

import com.scim_gateway.model.dto.LoginRequest;
import com.scim_gateway.model.dto.TokenResponse;
import com.scim_gateway.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and token generation endpoints")
public class AuthController {
    
    @Autowired
    private JwtService jwtService;
    
    // Default credentials for development
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";
    
    @Operation(
        summary = "Generate JWT Bearer token",
        description = "Authenticates user with username/password and returns a JWT Bearer token. " +
                     "Default credentials: username='admin', password='admin123'"
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "Login credentials (username and password)")
            @RequestBody LoginRequest request) {
        
        // Simple authentication (replace with real user validation in production)
        if (!authenticate(request.getUsername(), request.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        
        // Generate JWT token
        String token = jwtService.generateToken(request.getUsername(), Map.of(
            "role", "admin",
            "scope", "scim:full"
        ));
        
        TokenResponse response = new TokenResponse(
            token,
            request.getUsername(),
            jwtService.getExpiration()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Validate JWT token",
        description = "Checks if a JWT token is valid and returns the username"
    )
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @Parameter(description = "JWT token to validate")
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        
        if (token == null || !jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of(
                "valid", false,
                "message", "Invalid or expired token"
            ));
        }
        
        String username = jwtService.getUsernameFromToken(token);
        
        return ResponseEntity.ok(Map.of(
            "valid", true,
            "username", username,
            "message", "Token is valid"
        ));
    }
    
    @Operation(
        summary = "Get default credentials",
        description = "Returns the default login credentials for testing purposes"
    )
    @GetMapping("/credentials")
    public ResponseEntity<Map<String, String>> getCredentials() {
        return ResponseEntity.ok(Map.of(
            "username", DEFAULT_USERNAME,
            "password", DEFAULT_PASSWORD,
            "note", "Use these credentials with /auth/login endpoint"
        ));
    }
    
    /**
     * Simple authentication method
     * Replace this with real database validation in production
     */
    private boolean authenticate(String username, String password) {
        // Default admin credentials
        if (DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password)) {
            return true;
        }
        
        // You can add more users here or validate against database
        // Example: Check against MongoDB users collection
        
        return false;
    }
}
