package com.scim_gateway.model.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ScimPatchRequest {
    
    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp");
    
    @JsonProperty("Operations")
    private List<Operation> operations;
    
    @Data
    public static class Operation {
        @JsonProperty("op")
        private String op; // add, remove, replace
        
        @JsonProperty("path")
        private String path;
        
        @JsonProperty("value")
        private Object value; // Can be String, Map, or List depending on operation
    }
}
