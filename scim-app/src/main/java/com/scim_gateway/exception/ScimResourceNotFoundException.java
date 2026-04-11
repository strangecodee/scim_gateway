package com.scim_gateway.exception;

public class ScimResourceNotFoundException extends ScimException {
    
    public ScimResourceNotFoundException(String resourceType, String id) {
        super(resourceType + " not found with id: " + id, null, 404);
    }
}
