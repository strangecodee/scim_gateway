package com.scim_gateway.service;

import com.scim_gateway.model.mongo.MongoUser;
import com.scim_gateway.model.scim.ScimUser;
import com.scim_gateway.exception.ScimInvalidFilterException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ScimFilterParser {
    
    private static final Pattern FILTER_PATTERN = Pattern.compile(
        "(\\w+(?:\\.\\w+)?)\\s+(eq|ne|co|sw|ew|gt|ge|lt|le)\\s+\"([^\"]+)\""
    );
    
    public static List<MongoUser> applyFilter(List<MongoUser> users, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return users;
        }
        
        Stream<MongoUser> stream = users.stream();
        
        // Simple filter parsing - supports: attribute op "value"
        Matcher matcher = FILTER_PATTERN.matcher(filter);
        
        if (matcher.find()) {
            String attribute = matcher.group(1);
            String operator = matcher.group(2);
            String value = matcher.group(3);
            
            stream = stream.filter(user -> evaluateFilter(user, attribute, operator, value));
        }
        
        return stream.toList();
    }
    
    private static boolean evaluateFilter(MongoUser user, String attribute, String operator, String value) {
        String fieldValue = extractFieldValue(user, attribute);
        
        if (fieldValue == null) {
            return false;
        }
        
        return switch (operator.toLowerCase()) {
            case "eq" -> fieldValue.equalsIgnoreCase(value);
            case "ne" -> !fieldValue.equalsIgnoreCase(value);
            case "co" -> fieldValue.toLowerCase().contains(value.toLowerCase());
            case "sw" -> fieldValue.toLowerCase().startsWith(value.toLowerCase());
            case "ew" -> fieldValue.toLowerCase().endsWith(value.toLowerCase());
            case "gt" -> fieldValue.compareTo(value) > 0;
            case "ge" -> fieldValue.compareTo(value) >= 0;
            case "lt" -> fieldValue.compareTo(value) < 0;
            case "le" -> fieldValue.compareTo(value) <= 0;
            default -> throw new ScimInvalidFilterException("Unsupported operator: " + operator);
        };
    }
    
    private static String extractFieldValue(MongoUser user, String attribute) {
        return switch (attribute.toLowerCase()) {
            case "username", "userName" -> user.getUserName();
            case "email" -> user.getEmail();
            case "active" -> String.valueOf(user.isActive());
            default -> null;
        };
    }
    
    public static List<ScimUser> sortUsers(List<ScimUser> users, String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return users;
        }
        
        boolean ascending = "ascending".equalsIgnoreCase(sortOrder);
        
        return users.stream()
            .sorted((u1, u2) -> {
                String val1 = extractScimFieldValue(u1, sortBy);
                String val2 = extractScimFieldValue(u2, sortBy);
                
                if (val1 == null && val2 == null) return 0;
                if (val1 == null) return ascending ? -1 : 1;
                if (val2 == null) return ascending ? 1 : -1;
                
                int comparison = val1.compareToIgnoreCase(val2);
                return ascending ? comparison : -comparison;
            })
            .toList();
    }
    
    private static String extractScimFieldValue(ScimUser user, String attribute) {
        return switch (attribute.toLowerCase()) {
            case "username", "userName" -> user.getUserName();
            case "displayname", "displayName" -> user.getDisplayName();
            case "email" -> user.getEmails() != null && !user.getEmails().isEmpty() 
                ? user.getEmails().get(0).getValue() : null;
            case "active" -> String.valueOf(user.isActive());
            default -> null;
        };
    }
}
