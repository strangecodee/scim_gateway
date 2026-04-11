package com.scim_gateway.controller;

import com.scim_gateway.model.mongo.Application;
import com.scim_gateway.model.mongo.ProvisioningJob;
import com.scim_gateway.service.ApplicationRegistryService;
import com.scim_gateway.service.ProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scim/v2/apps")
@Tag(name = "Application Registry & Provisioning", description = "Manage external applications and automate user provisioning")
public class ProvisioningController {
    
    @Autowired
    private ApplicationRegistryService appRegistryService;
    
    @Autowired
    private ProvisioningService provisioningService;
    
    @Autowired
    private com.scim_gateway.repository.mongo.ProvisioningJobRepository jobRepository;
    
    // Application Management Endpoints
    
    @Operation(summary = "List all registered applications", description = "Returns all external applications registered for user provisioning")
    @GetMapping
    public List<Application> getAllApps() {
        return appRegistryService.getAllApps();
    }
    
    @Operation(summary = "Get application by ID", description = "Retrieves details of a specific registered application")
    @GetMapping("/{id}")
    public Application getApp(
            @Parameter(description = "Application ID") @PathVariable String id) {
        return appRegistryService.getAppById(id);
    }
    
    @Operation(summary = "Register new application", description = "Registers a new external application with API key authentication")
    @PostMapping
    public ResponseEntity<Application> registerApp(@RequestBody Application app) {
        Application created = appRegistryService.registerApp(app);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Operation(summary = "Update field mappings", description = "Configures attribute mapping rules for SCIM to app-specific fields")
    @PutMapping("/{id}/mappings")
    public ResponseEntity<Void> updateMappings(
            @Parameter(description = "Application ID") @PathVariable String id,
            @RequestBody Map<String, String> mappings) {
        appRegistryService.updateAppFieldMappings(id, mappings);
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Delete application", description = "Removes an application from the registry")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApp(@PathVariable String id) {
        appRegistryService.deleteApp(id);
        return ResponseEntity.noContent().build();
    }
    
    // Provisioning Endpoints
    
    @Operation(summary = "Provision user to application", description = "Creates user account in external application (async operation)")
    @PostMapping("/provision/{userId}/to/{appId}")
    public ResponseEntity<Map<String, String>> provisionUser(
            @Parameter(description = "User ID to provision") @PathVariable String userId,
            @Parameter(description = "Target application ID") @PathVariable String appId) {
        provisioningService.provisionUserToApp(userId, appId);
        return ResponseEntity.ok(Map.of(
            "status", "provisioning_started",
            "userId", userId,
            "appId", appId,
            "message", "User provisioning job created (async)"
        ));
    }
    
    @Operation(summary = "Deprovision user from application", description = "Deletes user account from external application")
    @PostMapping("/deprovision/{userId}/from/{appId}")
    public ResponseEntity<Map<String, String>> deprovisionUser(
            @Parameter(description = "User ID to deprovision") @PathVariable String userId,
            @Parameter(description = "Source application ID") @PathVariable String appId) {
        // Note: This would need to be implemented in ProvisioningService
        return ResponseEntity.ok(Map.of(
            "status", "deprovisioning_not_implemented",
            "message", "Deprovisioning endpoint - implement as needed"
        ));
    }
    
    // Job Tracking Endpoints
    
    @Operation(summary = "List all provisioning jobs", description = "Returns all provisioning jobs from database")
    @GetMapping("/jobs")
    public List<ProvisioningJob> getAllJobs() {
        return jobRepository.findAll();
    }
    
    @Operation(summary = "Get jobs by status", description = "Filters provisioning jobs by their current status")
    @GetMapping("/jobs/status/{status}")
    public List<ProvisioningJob> getJobsByStatus(
            @Parameter(description = "Job status: PENDING, RETRYING, SUCCESS, FAILED") 
            @PathVariable String status) {
        return jobRepository.findByStatus(ProvisioningJob.Status.valueOf(status.toUpperCase()));
    }
    
    @Operation(summary = "Get jobs for user", description = "Returns all provisioning jobs for a specific user")
    @GetMapping("/jobs/user/{userId}")
    public List<ProvisioningJob> getJobsForUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return provisioningService.getJobsByUser(userId);
    }
    
    @Operation(summary = "Get job details", description = "Retrieves detailed information about a specific provisioning job")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ProvisioningJob> getJob(
            @Parameter(description = "Job ID") @PathVariable String jobId) {
        return jobRepository.findById(jobId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
