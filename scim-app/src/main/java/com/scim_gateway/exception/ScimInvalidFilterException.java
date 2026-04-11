package com.scim_gateway.exception;

public class ScimInvalidFilterException extends ScimException {
    
    public ScimInvalidFilterException(String message) {
        super(message, "invalidFilter", 400);
    }
}
