package com.scim_gateway.repository.sql;

import com.scim_gateway.model.sql.SqlUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SQL User Repository - For JPA/PostgreSQL support
 * Currently inactive - kept for future SQL integration
 */
@Repository
public interface SqlUserRepository extends JpaRepository<SqlUser, String> {
    
    Optional<SqlUser> findByUserName(String userName);
    
    Optional<SqlUser> findByExternalId(String externalId);
    
    List<SqlUser> findByActive(Boolean active);
    
    boolean existsByUserName(String userName);
    
    boolean existsByEmail(String email);
}
