package com.scim_gateway.controller;

import com.scim_gateway.model.dto.LoginRequest;
import com.scim_gateway.model.dto.TokenResponse;
import com.scim_gateway.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and token generation endpoints")
public class AuthController {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Value("${auth.default.username:admin}")
    private String defaultUsername;
    
    @Value("${auth.default.password:admin123}")
    private String defaultPassword;
    
    @Value("${auth.enabled:true}")
    private boolean authEnabled;
    
    @Operation(
        summary = "Generate JWT Bearer token",
        description = "Authenticates user with username/password and returns a JWT Bearer token. " +
                     "Credentials are configured via environment variables or application.properties"
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "Login credentials (username and password)")
            @RequestBody LoginRequest request) {
        
        try {
            // Authenticate using Spring Security AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            // Get user roles/authorities
            String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
            
            // Generate JWT token with roles
            String token = jwtService.generateToken(request.getUsername(), Map.of(
                "role", roles,
                "scope", "scim:full"
            ));
            
            TokenResponse response = new TokenResponse(
                token,
                request.getUsername(),
                jwtService.getExpiration()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            // Invalid credentials
            return ResponseEntity.status(401).body(new TokenResponse(
                null,
                null,
                0,
                "Invalid username or password"
            ));
        } catch (Exception e) {
            // Other authentication errors
            return ResponseEntity.status(500).body(new TokenResponse(
                null,
                null,
                0,
                "Authentication failed: " + e.getMessage()
            ));
        }
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
        description = "Returns the default login credentials for testing purposes (only in development)"
    )
    @GetMapping("/credentials")
    public ResponseEntity<Map<String, Object>> getCredentials() {
        // Only return credentials in development mode
        if (!authEnabled) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Credentials endpoint disabled in production"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "username", defaultUsername,
            "password", "********", // Never expose password
            "note", "Use these credentials with /auth/login endpoint",
            "warning", "Change default credentials in production"
        ));
    }
    
}
