package com.scim_gateway.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Data
@Document(collection = "applications")
public class Application {
    
    @Id
    private String id;
    private String name;
    private String baseUrl;
    private String apiKey;
    private boolean enabled = true;
    
    // Field mappings: target_attribute -> source_attribute
    // Example: {"userName": "email", "displayName": "name.givenName"}
    private Map<String, String> fieldMappings;
    
    // Provisioning configuration
    private int syncIntervalMinutes = 5;
    private int maxRetries = 3;
    private boolean autoProvision = true;
    
    private String createdAt;
    private String updatedAt;
}
