package com.scim_gateway.client;

import com.scim_gateway.model.scim.ScimError;
import com.scim_gateway.model.scim.ScimUser;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalScimClient {
    
    private final RestTemplate restTemplate;
    
    public ExternalScimClient() {
        this.restTemplate = new RestTemplate();
    }
    
    public ScimResponse createUser(String baseUrl, String apiKey, ScimUser user) {
        String url = baseUrl + "/scim/v2/Users";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<ScimUser> request = new HttpEntity<>(user, headers);
        
        try {
            ResponseEntity<ScimUser> response = restTemplate.postForEntity(
                url, request, ScimUser.class
            );
            
            return new ScimResponse(
                response.getStatusCode().value(),
                response.getBody(),
                null
            );
        } catch (RestClientException e) {
            return handleError(e);
        }
    }
    
    public ScimResponse updateUser(String baseUrl, String apiKey, String userId, ScimUser user) {
        String url = baseUrl + "/scim/v2/Users/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<ScimUser> request = new HttpEntity<>(user, headers);
        
        try {
            ResponseEntity<ScimUser> response = restTemplate.exchange(
                url, HttpMethod.PUT, request, ScimUser.class
            );
            
            return new ScimResponse(
                response.getStatusCode().value(),
                response.getBody(),
                null
            );
        } catch (RestClientException e) {
            return handleError(e);
        }
    }
    
    public ScimResponse deleteUser(String baseUrl, String apiKey, String userId) {
        String url = baseUrl + "/scim/v2/Users/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.DELETE, request, Void.class
            );
            
            return new ScimResponse(
                response.getStatusCode().value(),
                null,
                null
            );
        } catch (RestClientException e) {
            return handleError(e);
        }
    }
    
    private ScimResponse handleError(Exception e) {
        ScimError error = new ScimError();
        error.setDetail(e.getMessage());
        error.setStatus("500");
        
        return new ScimResponse(500, null, error);
    }
    
    public static class ScimResponse {
        private final int statusCode;
        private final ScimUser user;
        private final ScimError error;
        
        public ScimResponse(int statusCode, ScimUser user, ScimError error) {
            this.statusCode = statusCode;
            this.user = user;
            this.error = error;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public ScimUser getUser() {
            return user;
        }
        
        public ScimError getError() {
            return error;
        }
    }
}
