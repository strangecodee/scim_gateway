package com.scim_gateway.adapter;

import com.scim_gateway.model.sql.SqlUser;
import com.scim_gateway.repository.sql.SqlUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * SQL User Adapter - For SQL/PostgreSQL database support
 * Currently inactive - kept for future SQL integration
 */
@Component
@Profile("sql") // Only active when 'sql' profile is enabled
public class SqlUserAdapter implements UserAdapter {
    
    @Autowired
    private SqlUserRepository sqlUserRepository;
    
    @Override
    public Object createUser(Object user) {
        // SQL implementation
        return null;
    }
    
    @Override
    public Optional<Object> getUser(String id) {
        // SQL implementation
        return Optional.empty();
    }
    
    @Override
    public List<Object> getAllUsers() {
        // SQL implementation
        return List.of();
    }
    
    @Override
    public Object updateUser(String id, Object user) {
        // SQL implementation
        return null;
    }
    
    @Override
    public void deleteUser(String id) {
        // SQL implementation
    }
    
    @Override
    public Optional<Object> getUserByUsername(String username) {
        // SQL implementation
        return Optional.empty();
    }
}
