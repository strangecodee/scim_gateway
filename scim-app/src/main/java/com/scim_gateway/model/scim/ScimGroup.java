package com.scim_gateway.model.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ScimGroup {
    
    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:Group");
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("externalId")
    private String externalId;
    
    @JsonProperty("displayName")
    private String displayName;
    
    @JsonProperty("members")
    private List<Member> members;
    
    @JsonProperty("meta")
    private Meta meta;
    
    @Data
    public static class Member {
        @JsonProperty("value")
        private String value;
        
        @JsonProperty("$ref")
        private String ref;
        
        @JsonProperty("display")
        private String display;
        
        @JsonProperty("type")
        private String type;
    }
    
    @Data
    public static class Meta {
        @JsonProperty("resourceType")
        private String resourceType;
        
        @JsonProperty("created")
        private String created;
        
        @JsonProperty("lastModified")
        private String lastModified;
        
        @JsonProperty("location")
        private String location;
        
        @JsonProperty("version")
        private String version;
    }
}
