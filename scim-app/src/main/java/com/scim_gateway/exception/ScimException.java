package com.scim_gateway.exception;

public class ScimException extends RuntimeException {
    
    private final String scimType;
    private final int statusCode;
    
    public ScimException(String message, String scimType, int statusCode) {
        super(message);
        this.scimType = scimType;
        this.statusCode = statusCode;
    }
    
    public String getScimType() {
        return scimType;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
