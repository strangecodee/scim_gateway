package com.scim_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scim/v2/ResourceTypes")
public class ScimResourceTypesController {
    
    @GetMapping
    public List<Map<String, Object>> getResourceTypes() {
        return List.of(getUserResourceType(), getGroupResourceType());
    }
    
    @GetMapping("/{name}")
    public Map<String, Object> getResourceType(@PathVariable String name) {
        if ("User".equalsIgnoreCase(name)) {
            return getUserResourceType();
        } else if ("Group".equalsIgnoreCase(name)) {
            return getGroupResourceType();
        }
        throw new RuntimeException("Resource type not found: " + name);
    }
    
    private Map<String, Object> getUserResourceType() {
        return Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ResourceType"),
            "id", "User",
            "name", "User",
            "endpoint", "/scim/v2/Users",
            "description", "User Account",
            "schema", "urn:ietf:params:scim:schemas:core:2.0:User",
            "schemaExtensions", List.of()
        );
    }
    
    private Map<String, Object> getGroupResourceType() {
        return Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ResourceType"),
            "id", "Group",
            "name", "Group",
            "endpoint", "/scim/v2/Groups",
            "description", "Group",
            "schema", "urn:ietf:params:scim:schemas:core:2.0:Group",
            "schemaExtensions", List.of()
        );
    }
}
