package com.scim_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scim")
public class ScimConfig {
    
    private int maxResults = 100;
    private int defaultCount = 20;
    private String baseUrl = "/scim/v2";
    
    private ProvisioningConfig provisioning = new ProvisioningConfig();
    
    public int getMaxResults() {
        return maxResults;
    }
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    public int getDefaultCount() {
        return defaultCount;
    }
    
    public void setDefaultCount(int defaultCount) {
        this.defaultCount = defaultCount;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public ProvisioningConfig getProvisioning() {
        return provisioning;
    }
    
    public void setProvisioning(ProvisioningConfig provisioning) {
        this.provisioning = provisioning;
    }
    
    public static class ProvisioningConfig {
        private String syncCron = "0 0/5 * * * *"; // Every 5 minutes
        private int maxRetries = 3;
        private int jobRetentionDays = 7;
        
        public String getSyncCron() {
            return syncCron;
        }
        
        public void setSyncCron(String syncCron) {
            this.syncCron = syncCron;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public int getJobRetentionDays() {
            return jobRetentionDays;
        }
        
        public void setJobRetentionDays(int jobRetentionDays) {
            this.jobRetentionDays = jobRetentionDays;
        }
    }
}
