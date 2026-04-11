package com.scim_gateway.service;

import com.scim_gateway.client.ExternalScimClient;
import com.scim_gateway.model.mongo.Application;
import com.scim_gateway.model.mongo.ProvisioningJob;
import com.scim_gateway.model.scim.ScimUser;
import com.scim_gateway.repository.mongo.ApplicationRepository;
import com.scim_gateway.repository.mongo.ProvisioningJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProvisioningService {
    
    @Autowired
    private ApplicationRepository appRepository;
    
    @Autowired
    private ProvisioningJobRepository jobRepository;
    
    @Autowired
    private ScimUserService userService;
    
    @Autowired
    private FieldMappingService fieldMappingService;
    
    @Autowired
    private ExternalScimClient externalClient;
    
    /**
     * Provision a user to a specific application
     */
    @Async
    public void provisionUserToApp(String userId, String appId) {
        Application app = appRepository.findById(appId)
            .orElseThrow(() -> new RuntimeException("Application not found: " + appId));
        
        if (!app.isEnabled()) {
            return; // Skip disabled apps
        }
        
        ScimUser user = userService.getUser(userId);
        
        // Create provisioning job
        ProvisioningJob job = new ProvisioningJob();
        job.setId(UUID.randomUUID().toString());
        job.setUserId(userId);
        job.setApplicationId(appId);
        job.setOperation(ProvisioningJob.Operation.CREATE);
        job.setStatus(ProvisioningJob.Status.PENDING);
        job.setAttempts(0);
        job.setMaxRetries(app.getMaxRetries());
        job.setCreatedAt(Instant.now().toString());
        
        jobRepository.save(job);
        
        // Execute provisioning
        executeProvisioning(job, user, app);
    }
    
    /**
     * Provision user to all enabled applications
     */
    @Async
    public void provisionUserToAllApps(String userId) {
        List<Application> apps = appRepository.findByAutoProvisionTrueAndEnabledTrue();
        
        for (Application app : apps) {
            provisionUserToApp(userId, app.getId());
        }
    }
    
    /**
     * Sync all users to all apps
     */
    @Async
    public void syncAllUsers() {
        List<Application> apps = appRepository.findByEnabledTrue();
        
        for (Application app : apps) {
            // Get all users and provision to this app
            // This would typically use pagination for large datasets
            var userList = userService.listUsers(1, 1000, null, null, null);
            
            for (ScimUser user : userList.getResources()) {
                provisionUserToApp(user.getId(), app.getId());
            }
        }
    }
    
    /**
     * Get provisioning jobs for a user
     */
    public List<ProvisioningJob> getJobsByUser(String userId) {
        return jobRepository.findByUserId(userId);
    }
    
    /**
     * Execute the actual provisioning with retry logic
     */
    private void executeProvisioning(ProvisioningJob job, ScimUser user, Application app) {
        job.setStatus(ProvisioningJob.Status.RETRYING);
        job.setAttempts(job.getAttempts() + 1);
        job.setUpdatedAt(Instant.now().toString());
        jobRepository.save(job);
        
        try {
            // Transform user based on app field mappings
            ScimUser transformedUser = fieldMappingService.transformUserForApp(user, app);
            
            // Send to external application
            ExternalScimClient.ScimResponse response;
            
            switch (job.getOperation()) {
                case CREATE -> response = externalClient.createUser(app.getBaseUrl(), app.getApiKey(), transformedUser);
                case UPDATE -> response = externalClient.updateUser(app.getBaseUrl(), app.getApiKey(), user.getId(), transformedUser);
                case DELETE -> response = externalClient.deleteUser(app.getBaseUrl(), app.getApiKey(), user.getId());
                default -> throw new RuntimeException("Unknown operation: " + job.getOperation());
            }
            
            if (response.isSuccess()) {
                job.setStatus(ProvisioningJob.Status.SUCCESS);
                job.setCompletedAt(Instant.now().toString());
                job.setResponseData("Status: " + response.getStatusCode());
            } else {
                throw new RuntimeException("External API returned: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            job.setErrorMessage(e.getMessage());
            
            if (job.getAttempts() < job.getMaxRetries()) {
                job.setStatus(ProvisioningJob.Status.FAILED); // Will be retried by scheduled job
            } else {
                job.setStatus(ProvisioningJob.Status.FAILED);
                job.setCompletedAt(Instant.now().toString());
            }
        }
        
        job.setUpdatedAt(Instant.now().toString());
        jobRepository.save(job);
    }
    
    /**
     * Retry failed jobs
     */
    public void retryFailedJobs() {
        List<ProvisioningJob> failedJobs = jobRepository.findByStatus(ProvisioningJob.Status.FAILED);
        
        for (ProvisioningJob job : failedJobs) {
            if (job.getAttempts() < job.getMaxRetries()) {
                try {
                    ScimUser user = userService.getUser(job.getUserId());
                    Application app = appRepository.findById(job.getApplicationId()).orElse(null);
                    
                    if (app != null) {
                        executeProvisioning(job, user, app);
                    }
                } catch (Exception e) {
                    // Log error, job will be retried again later
                }
            }
        }
    }
}
