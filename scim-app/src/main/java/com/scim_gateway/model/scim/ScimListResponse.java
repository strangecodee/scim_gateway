package com.scim_gateway.model.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ScimListResponse<T> {
    
    @JsonProperty("schemas")
    private List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse");
    
    @JsonProperty("totalResults")
    private int totalResults;
    
    @JsonProperty("startIndex")
    private int startIndex = 1;
    
    @JsonProperty("itemsPerPage")
    private int itemsPerPage;
    
    @JsonProperty("Resources")
    private List<T> resources;
}
