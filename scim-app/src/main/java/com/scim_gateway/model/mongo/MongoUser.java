package com.scim_gateway.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class MongoUser {
    
    @Id
    private String id;
    private String userName;
    private String email;
    private boolean active = true;
    private String externalId;
    
    // Meta information for SCIM
    private String metaCreatedAt;
    private String metaLastModified;
    private String metaLocation;
}
