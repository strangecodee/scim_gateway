package com.scim_gateway.controller;

import com.scim_gateway.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scim/v2/ServiceProviderConfig")
public class ScimConfigController {
    
    @Autowired
    private AppProperties appProperties;
    
    @GetMapping
    public Map<String, Object> getConfig() {
        return Map.of(
            "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
            "documentationUri", appProperties.getDocumentation().getBaseUrl(),
            "patch", Map.of("supported", true),
            "bulk", Map.of("supported", false, "maxOperations", 0, "maxPayloadSize", 0),
            "filter", Map.of("supported", true, "maxResults", 100),
            "changePassword", Map.of("supported", false),
            "sort", Map.of("supported", true),
            "etag", Map.of("supported", false),
            "authenticationSchemes", List.of(
                Map.of(
                    "name", "OAuth Bearer Token",
                    "description", "Authentication scheme using the OAuth Bearer Token Standard",
                    "specUri", appProperties.getAuthentication().getSpecUri(),
                    "documentationUri", appProperties.getAuthentication().getDocumentationUri(),
                    "type", appProperties.getAuthentication().getTokenType(),
                    "primary", true
                ),
                Map.of(
                    "name", "Basic Authentication",
                    "description", "Authentication scheme using Basic Authentication",
                    "specUri", appProperties.getAuthentication().getBasicSpecUri(),
                    "documentationUri", appProperties.getAuthentication().getDocumentationUri(),
                    "type", "basic",
                    "primary", false
                )
            )
        );
    }
}
