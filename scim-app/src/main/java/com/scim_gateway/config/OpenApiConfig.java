package com.scim_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Reads all content from swagger-config.properties for easy customization
 */
@Configuration
@PropertySource("classpath:swagger-config.properties")
public class OpenApiConfig {
    
    @Autowired
    private AppProperties appProperties;
    
    // Swagger Configuration Properties from swagger-config.properties
    @Value("${swagger.api.title:SCIM Gateway - REST API Documentation}")
    private String apiTitle;
    
    @Value("${swagger.api.version:1.0.0}")
    private String apiVersion;
    
    @Value("${swagger.api.description:}")
    private String apiDescription;
    
    @Value("${swagger.api.terms-of-service:}")
    private String termsOfService;
    
    @Value("${swagger.contact.name:}")
    private String contactName;
    
    @Value("${swagger.contact.email:}")
    private String contactEmail;
    
    @Value("${swagger.contact.url:}")
    private String contactUrl;
    
    @Value("${swagger.license.name:Apache 2.0}")
    private String licenseName;
    
    @Value("${swagger.license.url:}")
    private String licenseUrl;
    
    @Value("${swagger.external-docs.description:Complete SCIM Gateway Documentation}")
    private String externalDocsDescription;
    
    @Value("${swagger.external-docs.url:}")
    private String externalDocsUrl;
    
    @Value("${swagger.security.scheme-name:bearerAuth}")
    private String securitySchemeName;
    
    @Value("${swagger.security.description:Enter your JWT Bearer token for authentication.}")
    private String securityDescription;
    
    @Value("${swagger.server.url:}")
    private String serverUrl;
    
    @Value("${swagger.server.description:Local Development Server}")
    private String serverDescription;
    
    @Value("${swagger.tags.Authentication:🔐 User authentication and JWT token management}")
    private String tagAuthentication;
    
    @Value("${swagger.tags.SCIM Users:👤 SCIM 2.0 compliant user management operations}")
    private String tagSCIMUsers;
    
    @Value("${swagger.tags.SCIM Groups:👥 Group management and membership operations}")
    private String tagSCIMGroups;
    
    @Value("${swagger.tags.Service Config:⚙️ SCIM service provider configuration and capabilities}")
    private String tagServiceConfig;
    
    @Value("${swagger.tags.Resource Types:📄 Available SCIM resource types and their schemas}")
    private String tagResourceTypes;
    
    @Value("${swagger.tags.Schemas:📋 SCIM schema definitions (RFC 7643)}")
    private String tagSchemas;
    
    @Value("${swagger.tags.Applications:📱 External application registration and API key management}")
    private String tagApplications;
    
    @Value("${swagger.tags.Provisioning:⚡ User provisioning engine and job tracking}")
    private String tagProvisioning;
    
    @Bean
    public OpenAPI scimGatewayOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(apiTitle)
                .version(apiVersion)
                .description(apiDescription.isEmpty() ? buildDefaultDescription() : apiDescription)
                .contact(new Contact()
                    .name(contactName)
                    .email(contactEmail)
                    .url(contactUrl))
                .license(new License()
                    .name(licenseName)
                    .url(licenseUrl))
                .termsOfService(termsOfService))
            .servers(buildServers())
            .externalDocs(buildExternalDocs())
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(securityDescription)))
            .tags(buildTags());
    }
    
    /**
     * Build servers list
     * If no server URL is configured, uses relative path (works for any deployment)
     */
    private List<Server> buildServers() {
        List<Server> servers = new ArrayList<>();
        
        // If serverUrl is empty or not set, use relative URL (auto-detects host)
        String url = (serverUrl == null || serverUrl.trim().isEmpty()) 
            ? "/" 
            : serverUrl;
        
        servers.add(new Server()
            .url(url)
            .description(serverDescription));
        
        return servers;
    }
    
    /**
     * Build external documentation
     */
    private ExternalDocumentation buildExternalDocs() {
        if (externalDocsUrl.isEmpty()) {
            return null;
        }
        return new ExternalDocumentation()
            .description(externalDocsDescription)
            .url(externalDocsUrl);
    }
    
    /**
     * Build organized API tags from properties
     */
    private List<Tag> buildTags() {
        List<Tag> tags = new ArrayList<>();
        
        tags.add(new Tag().name("Authentication").description(tagAuthentication));
        tags.add(new Tag().name("SCIM Users").description(tagSCIMUsers));
        tags.add(new Tag().name("SCIM Groups").description(tagSCIMGroups));
        tags.add(new Tag().name("Service Config").description(tagServiceConfig));
        tags.add(new Tag().name("Resource Types").description(tagResourceTypes));
        tags.add(new Tag().name("Schemas").description(tagSchemas));
        tags.add(new Tag().name("Applications").description(tagApplications));
        tags.add(new Tag().name("Provisioning").description(tagProvisioning));
        
        return tags;
    }
    
    /**
     * Build default description if not provided in properties
     */
    private String buildDefaultDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("## 🚀 SCIM 2.0 Gateway - Complete Identity Management Solution\n\n");
        desc.append("A production-ready SCIM (System for Cross-domain Identity Management) 2.0 compliant API.\n\n");
        desc.append("### 🔐 Authentication\n\n");
        desc.append("All endpoints require JWT Bearer token authentication.\n\n");
        desc.append("### 📖 API Sections\n\n");
        desc.append("- **Authentication** - Login and token management\n");
        desc.append("- **SCIM Users** - User CRUD operations\n");
        desc.append("- **SCIM Groups** - Group management\n");
        desc.append("- **Provisioning** - Automated user provisioning\n\n");
        desc.append("### 📞 Support\n\n");
        desc.append("- **Contact**: " + contactName + "\n");
        desc.append("- **Email**: " + contactEmail + "\n");
        desc.append("- **GitHub**: " + contactUrl + "\n");
        
        return desc.toString();
    }
}
