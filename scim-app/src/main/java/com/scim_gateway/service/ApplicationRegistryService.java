package com.scim_gateway.service;

import com.scim_gateway.model.mongo.Application;
import com.scim_gateway.repository.mongo.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicationRegistryService {
    
    @Autowired
    private ApplicationRepository appRepository;
    
    public List<Application> getAllEnabledApps() {
        return appRepository.findByEnabledTrue();
    }
    
    public List<Application> getAllApps() {
        return appRepository.findAll();
    }
    
    public Application registerApp(Application app) {
        if (app.getId() == null || app.getId().isEmpty()) {
            app.setId(UUID.randomUUID().toString());
        }
        
        String now = Instant.now().toString();
        app.setCreatedAt(now);
        app.setUpdatedAt(now);
        
        return appRepository.save(app);
    }
    
    public Application getAppById(String appId) {
        return appRepository.findById(appId)
            .orElseThrow(() -> new RuntimeException("Application not found: " + appId));
    }
    
    public void updateAppFieldMappings(String appId, Map<String, String> mappings) {
        Application app = getAppById(appId);
        app.setFieldMappings(mappings);
        app.setUpdatedAt(Instant.now().toString());
        appRepository.save(app);
    }
    
    public void enableApp(String appId) {
        Application app = getAppById(appId);
        app.setEnabled(true);
        app.setUpdatedAt(Instant.now().toString());
        appRepository.save(app);
    }
    
    public void disableApp(String appId) {
        Application app = getAppById(appId);
        app.setEnabled(false);
        app.setUpdatedAt(Instant.now().toString());
        appRepository.save(app);
    }
    
    public void deleteApp(String appId) {
        appRepository.deleteById(appId);
    }
}
