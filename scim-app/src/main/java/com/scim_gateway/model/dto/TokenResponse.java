package com.scim_gateway.model.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    
    public TokenResponse(String token, String username, long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.username = username;
    }
}
