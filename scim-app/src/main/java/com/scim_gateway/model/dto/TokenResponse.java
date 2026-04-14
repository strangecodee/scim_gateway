package com.scim_gateway.model.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    private String error;
    
    public TokenResponse(String token, String username, long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.username = username;
        this.error = null;
    }
    
    public TokenResponse(String token, String username, long expiresIn, String error) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.username = username;
        this.error = error;
    }
}
