package com.scim_gateway.model.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ScimError {
    
    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:Error");
    
    @JsonProperty("detail")
    private String detail;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("scimType")
    private String scimType;
}
