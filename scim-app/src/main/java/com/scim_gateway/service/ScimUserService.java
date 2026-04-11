package com.scim_gateway.service;

import com.scim_gateway.adapter.InMemoryUserAdapter;
import com.scim_gateway.adapter.UserAdapter;
import com.scim_gateway.model.mongo.MongoUser;
import com.scim_gateway.model.scim.ScimListResponse;
import com.scim_gateway.model.scim.ScimPatchRequest;
import com.scim_gateway.model.scim.ScimUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ScimUserService {
    
    @Autowired
    private UserAdapter userAdapter;
    
    public ScimUser createUser(ScimUser scimUser) {
        // Validate required fields
        if (scimUser.getUserName() == null || scimUser.getUserName().trim().isEmpty()) {
            throw new RuntimeException("userName is required");
        }
        
        // Convert SCIM user to MongoUser
        MongoUser mongoUser = fromScimUser(scimUser);
        
        // Generate ID if not present
        if (mongoUser.getId() == null || mongoUser.getId().isEmpty()) {
            mongoUser.setId(UUID.randomUUID().toString());
        }
        
        // Set meta information
        String now = Instant.now().toString();
        mongoUser.setMetaCreatedAt(now);
        mongoUser.setMetaLastModified(now);
        mongoUser.setMetaLocation("/scim/v2/Users/" + mongoUser.getId());
        
        // Save to database
        MongoUser saved = userAdapter.save(mongoUser);
        
        // Convert back to SCIM user
        return toScimUser(saved);
    }
    
    public ScimUser getUser(String id) {
        Optional<MongoUser> mongoUser = userAdapter.findById(id);
        
        if (mongoUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        return toScimUser(mongoUser.get());
    }
    
    public ScimListResponse<ScimUser> listUsers(int startIndex, int count, String filter, String sortBy, String sortOrder) {
        // Get all users from adapter
        List<MongoUser> allUsers = userAdapter.findAll();
        
        // Apply filter if present
        if (filter != null && !filter.trim().isEmpty()) {
            allUsers = ScimFilterParser.applyFilter(allUsers, filter);
        }
        
        // Get total results before pagination
        int totalResults = allUsers.size();
        
        // Convert to SCIM users
        List<ScimUser> scimUsers = allUsers.stream()
            .map(this::toScimUser)
            .collect(Collectors.toList());
        
        // Apply sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            scimUsers = ScimFilterParser.sortUsers(scimUsers, sortBy, sortOrder);
        }
        
        // Apply pagination
        int fromIndex = Math.max(0, startIndex - 1);
        int toIndex = Math.min(fromIndex + count, scimUsers.size());
        
        List<ScimUser> paginatedUsers = fromIndex < scimUsers.size() 
            ? scimUsers.subList(fromIndex, toIndex) 
            : new ArrayList<>();
        
        // Build response
        ScimListResponse<ScimUser> response = new ScimListResponse<>();
        response.setTotalResults(totalResults);
        response.setStartIndex(startIndex);
        response.setItemsPerPage(paginatedUsers.size());
        response.setResources(paginatedUsers);
        
        return response;
    }
    
    public ScimUser updateUser(String id, ScimUser scimUser) {
        // Check if user exists
        Optional<MongoUser> existing = userAdapter.findById(id);
        
        if (existing.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        // Convert SCIM user to MongoUser
        MongoUser mongoUser = fromScimUser(scimUser);
        mongoUser.setId(id); // Ensure ID is preserved
        
        // Update meta
        mongoUser.setMetaLastModified(Instant.now().toString());
        
        // Save
        MongoUser saved = userAdapter.save(mongoUser);
        
        return toScimUser(saved);
    }
    
    public ScimUser patchUser(String id, ScimPatchRequest patchRequest) {
        // Get existing user
        Optional<MongoUser> existing = userAdapter.findById(id);
        
        if (existing.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        MongoUser mongoUser = existing.get();
        
        // Apply patch operations
        for (ScimPatchRequest.Operation op : patchRequest.getOperations()) {
            applyOperation(mongoUser, op);
        }
        
        // Update meta
        mongoUser.setMetaLastModified(Instant.now().toString());
        
        // Save
        MongoUser saved = userAdapter.save(mongoUser);
        
        return toScimUser(saved);
    }
    
    public void deleteUser(String id) {
        Optional<MongoUser> existing = userAdapter.findById(id);
        
        if (existing.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        // SCIM recommends soft delete - set active to false
        MongoUser user = existing.get();
        user.setActive(false);
        user.setMetaLastModified(Instant.now().toString());
        userAdapter.save(user);
    }
    
    // Helper methods
    
    private void applyOperation(MongoUser user, ScimPatchRequest.Operation op) {
        String opType = op.getOp().toLowerCase();
        
        switch (opType) {
            case "replace":
            case "add":
                applyAddOrReplace(user, op);
                break;
            case "remove":
                applyRemove(user, op);
                break;
            default:
                throw new RuntimeException("Unsupported patch operation: " + op.getOp());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void applyAddOrReplace(MongoUser user, ScimPatchRequest.Operation op) {
        String path = op.getPath();
        Object value = op.getValue();
        
        if (path == null || path.isEmpty()) {
            // If no path, value should be an object with attributes
            if (value instanceof Map) {
                Map<String, Object> attrs = (Map<String, Object>) value;
                if (attrs.containsKey("userName")) {
                    user.setUserName((String) attrs.get("userName"));
                }
                if (attrs.containsKey("email")) {
                    user.setEmail((String) attrs.get("email"));
                }
                if (attrs.containsKey("active")) {
                    user.setActive((Boolean) attrs.get("active"));
                }
            }
        } else {
            // Handle specific paths
            switch (path.toLowerCase()) {
                case "username":
                    user.setUserName(value.toString());
                    break;
                case "email":
                case "emails":
                    if (value instanceof String) {
                        user.setEmail((String) value);
                    } else if (value instanceof List) {
                        List<?> emails = (List<?>) value;
                        if (!emails.isEmpty() && emails.get(0) instanceof Map) {
                            Map<?, ?> emailObj = (Map<?, ?>) emails.get(0);
                            user.setEmail(emailObj.get("value").toString());
                        }
                    }
                    break;
                case "active":
                    user.setActive(Boolean.parseBoolean(value.toString()));
                    break;
                case "displayname":
                    if (value instanceof Map) {
                        Map<?, ?> nameObj = (Map<?, ?>) value;
                        // For now, we don't have separate name fields in MongoUser
                    }
                    break;
            }
        }
    }
    
    private void applyRemove(MongoUser user, ScimPatchRequest.Operation op) {
        String path = op.getPath();
        
        if (path != null && path.toLowerCase().equals("active")) {
            user.setActive(false);
        }
        // Add more remove logic as needed
    }
    
    private MongoUser fromScimUser(ScimUser scimUser) {
        MongoUser mongoUser = new MongoUser();
        
        mongoUser.setId(scimUser.getId());
        mongoUser.setUserName(scimUser.getUserName());
        mongoUser.setActive(scimUser.isActive());
        mongoUser.setExternalId(scimUser.getExternalId());
        
        // Extract primary email
        if (scimUser.getEmails() != null && !scimUser.getEmails().isEmpty()) {
            ScimUser.Email primaryEmail = scimUser.getEmails().stream()
                .filter(ScimUser.Email::isPrimary)
                .findFirst()
                .orElse(scimUser.getEmails().get(0));
            mongoUser.setEmail(primaryEmail.getValue());
        }
        
        return mongoUser;
    }
    
    private ScimUser toScimUser(MongoUser mongoUser) {
        ScimUser scimUser = new ScimUser();
        
        scimUser.setId(mongoUser.getId());
        scimUser.setUserName(mongoUser.getUserName());
        scimUser.setActive(mongoUser.isActive());
        scimUser.setExternalId(mongoUser.getExternalId());
        
        // Set email
        if (mongoUser.getEmail() != null) {
            ScimUser.Email email = new ScimUser.Email();
            email.setValue(mongoUser.getEmail());
            email.setPrimary(true);
            email.setType("work");
            scimUser.setEmails(List.of(email));
        }
        
        // Set meta
        ScimUser.Meta meta = new ScimUser.Meta();
        meta.setResourceType("User");
        meta.setCreated(mongoUser.getMetaCreatedAt());
        meta.setLastModified(mongoUser.getMetaLastModified());
        meta.setLocation(mongoUser.getMetaLocation());
        scimUser.setMeta(meta);
        
        return scimUser;
    }
}
