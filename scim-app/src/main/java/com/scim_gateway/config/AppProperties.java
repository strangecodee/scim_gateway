package com.scim_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Application properties - All values must be configured via environment variables or application.properties
 * NO hardcoded URLs or sensitive data
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private Info info = new Info();
    private Documentation documentation = new Documentation();
    private Authentication authentication = new Authentication();
    
    @Data
    public static class Info {
        // Must be set via APP_NAME environment variable
        private String name = "";
        private String version = "";
        private String description = "";
        private String vendor = "";
    }
    
    @Data
    public static class Documentation {
        // Must be set via DOC_BASE_URL environment variable
        private String baseUrl = "";
        private String apiDocs = "/api-docs";
        private String swaggerUi = "/swagger-ui.html";
        private List<String> supportEmails = List.of();
    }
    
    @Data
    public static class Authentication {
        private String tokenType = "oauthbearertoken";
        // Must be set via environment variables
        private String specUri = "";
        private String documentationUri = "";
        private String basicSpecUri = "";
        private String licenseUrl = "";
        private String licenseName = "";
    }
}
