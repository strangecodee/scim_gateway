package com.scim_gateway.exception;

public class ScimAlreadyExistsException extends ScimException {
    
    public ScimAlreadyExistsException(String resourceType, String attribute, String value) {
        super(resourceType + " already exists with " + attribute + ": " + value, "uniqueness", 409);
    }
}
