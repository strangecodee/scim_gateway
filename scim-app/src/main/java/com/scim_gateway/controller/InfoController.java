package com.scim_gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Application Information Controller
 * Provides basic API information and metadata
 */
@RestController
@RequestMapping("/api")
@Tag(name = "API Info", description = "Application information and metadata")
public class InfoController {
    
    @Value("${app.info.name:SCIM Gateway}")
    private String appName;
    
    @Value("${app.info.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.info.description:SCIM 2.0 compliant identity management gateway}")
    private String appDescription;
    
    @Value("${app.info.vendor:Anurag Maurya}")
    private String appVendor;
    
    @Value("${spring.application.name:scim-app}")
    private String serviceName;
    
    @Operation(
        summary = "Get API information",
        description = "Returns basic information about the SCIM Gateway API including name, version, and vendor"
    )
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
            "name", appName,
            "version", appVersion,
            "description", appDescription,
            "vendor", appVendor,
            "service", serviceName,
            "status", "running"
        ));
    }
}
