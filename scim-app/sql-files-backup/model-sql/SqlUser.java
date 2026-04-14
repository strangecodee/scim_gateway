package com.scim_gateway.model.sql;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * SQL User Entity - For PostgreSQL/MySQL support
 * Currently inactive - kept for future SQL integration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class SqlUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String userName;
    
    private String externalId;
    
    private String displayName;
    
    private String givenName;
    
    private String familyName;
    
    private String email;
    
    private Boolean active = true;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
