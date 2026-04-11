package com.scim_gateway.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "provisioning_jobs")
public class ProvisioningJob {
    
    @Id
    private String id;
    private String applicationId;
    private String userId;
    
    public enum Status {
        PENDING, SUCCESS, FAILED, RETRYING
    }
    
    public enum Operation {
        CREATE, UPDATE, DELETE
    }
    
    private Status status = Status.PENDING;
    private Operation operation;
    private int attempts = 0;
    private int maxRetries = 3;
    private String errorMessage;
    private String responseData;
    
    private String createdAt;
    private String updatedAt;
    private String completedAt;
}
