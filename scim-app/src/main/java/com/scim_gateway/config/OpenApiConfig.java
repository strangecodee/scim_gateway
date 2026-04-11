package com.scim_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI scimGatewayOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("SCIM 2.0 Gateway API")
                .version("1.0.0")
                .description("## SCIM (System for Cross-domain Identity Management) Gateway\n\n" +
                    "A comprehensive SCIM 2.0 compliant API for user provisioning and identity management.\n\n" +
                    "### Features\n" +
                    "- ✅ **User Management**: Create, Read, Update, Delete users\n" +
                    "- ✅ **Group Management**: Manage user groups and memberships\n" +
                    "- ✅ **Filtering**: Advanced SCIM filter expressions\n" +
                    "- ✅ **Multi-App Support**: Register and manage external applications\n" +
                    "- ✅ **Provisioning Engine**: Automated user provisioning to external apps\n" +
                    "- ✅ **Field Mapping**: Custom attribute transformation per application\n" +
                    "- ✅ **Job Tracking**: Monitor provisioning jobs and sync status\n\n" +
                    "### SCIM 2.0 Compliance\n" +
                    "- RFC 7643: SCIM Core Schema\n" +
                    "- RFC 7644: SCIM Protocol\n\n" +
                    "### Authentication\n" +
                    "Click the **Authorize** button above to set your Bearer token.")
                .contact(new Contact()
                    .name("SCIM Gateway Team")
                    .email("admin@scimgateway.com")
                    .url("https://github.com/scim-gateway"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your Bearer token for authentication")));
    }
}
