package com.scim_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scim/v2/Schemas")
public class ScimSchemaController {
    
    @GetMapping
    public List<Map<String, Object>> getSchemas() {
        return List.of(getUserSchema(), getGroupSchema());
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getSchema(@PathVariable String id) {
        if ("urn:ietf:params:scim:schemas:core:2.0:User".equals(id)) {
            return getUserSchema();
        } else if ("urn:ietf:params:scim:schemas:core:2.0:Group".equals(id)) {
            return getGroupSchema();
        }
        throw new RuntimeException("Schema not found: " + id);
    }
    
    private Map<String, Object> getUserSchema() {
        return Map.of(
            "id", "urn:ietf:params:scim:schemas:core:2.0:User",
            "name", "User",
            "description", "User Account",
            "attributes", List.of(
                Map.of("name", "userName", "type", "string", "required", true, "mutability", "readWrite"),
                Map.of("name", "name", "type", "complex", "required", false, "mutability", "readWrite"),
                Map.of("name", "emails", "type", "complex", "multiValued", true, "required", false),
                Map.of("name", "active", "type", "boolean", "required", false, "mutability", "readWrite")
            )
        );
    }
    
    private Map<String, Object> getGroupSchema() {
        return Map.of(
            "id", "urn:ietf:params:scim:schemas:core:2.0:Group",
            "name", "Group",
            "description", "Group",
            "attributes", List.of(
                Map.of("name", "displayName", "type", "string", "required", true, "mutability", "readWrite"),
                Map.of("name", "members", "type", "complex", "multiValued", true, "required", false)
            )
        );
    }
}
