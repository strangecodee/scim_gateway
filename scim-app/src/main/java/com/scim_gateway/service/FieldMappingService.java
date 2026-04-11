package com.scim_gateway.service;

import com.scim_gateway.model.mongo.Application;
import com.scim_gateway.model.scim.ScimUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FieldMappingService {
    
    /**
     * Transform a SCIM user based on application-specific field mappings
     * 
     * @param user Original SCIM user
     * @param app Application with field mappings
     * @return Transformed SCIM user
     */
    public ScimUser transformUserForApp(ScimUser user, Application app) {
        if (app.getFieldMappings() == null || app.getFieldMappings().isEmpty()) {
            return user; // No mappings, return as-is
        }
        
        ScimUser transformed = new ScimUser();
        transformed.setId(user.getId());
        transformed.setExternalId(user.getExternalId());
        transformed.setSchemas(user.getSchemas());
        transformed.setActive(user.isActive());
        
        Map<String, String> mappings = app.getFieldMappings();
        
        // Apply each mapping
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String targetField = entry.getKey();
            String sourcePath = entry.getValue();
            
            Object value = extractValue(user, sourcePath);
            setFieldValue(transformed, targetField, value);
        }
        
        return transformed;
    }
    
    /**
     * Extract value from SCIM user using dot notation path
     * Example: "name.givenName", "emails[0].value"
     */
    private Object extractValue(ScimUser user, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        // Handle nested paths
        String[] parts = path.split("\\.");
        Object current = user;
        
        for (String part : parts) {
            if (current == null) return null;
            
            // Handle array notation
            if (part.contains("[")) {
                String fieldName = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));
                
                Object fieldValue = getFieldValue(current, fieldName);
                if (fieldValue instanceof List && index < ((List<?>) fieldValue).size()) {
                    current = ((List<?>) fieldValue).get(index);
                } else {
                    return null;
                }
            } else {
                current = getFieldValue(current, part);
            }
        }
        
        return current;
    }
    
    /**
     * Get field value from object using reflection-like approach
     */
    @SuppressWarnings("unchecked")
    private Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) return null;
        
        if (obj instanceof ScimUser user) {
            return switch (fieldName.toLowerCase()) {
                case "id" -> user.getId();
                case "externalid" -> user.getExternalId();
                case "username" -> user.getUserName();
                case "displayname" -> user.getDisplayName();
                case "name" -> user.getName();
                case "emails" -> user.getEmails();
                case "phonenumbers" -> user.getPhoneNumbers();
                case "active" -> user.isActive();
                case "title" -> user.getTitle();
                case "usertype" -> user.getUserType();
                default -> null;
            };
        } else if (obj instanceof ScimUser.Name name) {
            return switch (fieldName.toLowerCase()) {
                case "formatted" -> name.getFormatted();
                case "givenname" -> name.getGivenName();
                case "familyname" -> name.getFamilyName();
                case "middlename" -> name.getMiddleName();
                default -> null;
            };
        } else if (obj instanceof ScimUser.Email email) {
            return switch (fieldName.toLowerCase()) {
                case "value" -> email.getValue();
                case "type" -> email.getType();
                case "primary" -> email.isPrimary();
                default -> null;
            };
        } else if (obj instanceof Map map) {
            return map.get(fieldName);
        }
        
        return null;
    }
    
    /**
     * Set field value on SCIM user
     */
    private void setFieldValue(ScimUser user, String fieldName, Object value) {
        if (user == null || fieldName == null || value == null) return;
        
        String strValue = value.toString();
        
        switch (fieldName.toLowerCase()) {
            case "username" -> user.setUserName(strValue);
            case "displayname" -> user.setDisplayName(strValue);
            case "title" -> user.setTitle(strValue);
            case "usertype" -> user.setUserType(strValue);
            case "preferredlanguage" -> user.setPreferredLanguage(strValue);
            case "locale" -> user.setLocale(strValue);
            case "timezone" -> user.setTimezone(strValue);
            case "nickname" -> user.setNickName(strValue);
            case "profileurl" -> user.setProfileUrl(strValue);
            case "active" -> {
                if (value instanceof Boolean bool) {
                    user.setActive(bool);
                } else {
                    user.setActive(Boolean.parseBoolean(strValue));
                }
            }
            case "email", "emails" -> {
                // Simple case: set primary email
                if (user.getEmails() == null) {
                    user.setEmails(List.of());
                }
                // For complex mappings, would need more logic
            }
            case "externalid" -> user.setExternalId(strValue);
            // Add more field mappings as needed
        }
    }
}
