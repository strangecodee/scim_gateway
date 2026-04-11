package com.scim_gateway.controller;

import com.scim_gateway.model.scim.ScimListResponse;
import com.scim_gateway.model.scim.ScimPatchRequest;
import com.scim_gateway.model.scim.ScimUser;
import com.scim_gateway.service.ScimUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scim/v2/Users")
@Tag(name = "SCIM Users", description = "User management operations following SCIM 2.0 specification (RFC 7643)")
public class ScimUserController {
    
    @Autowired
    private ScimUserService userService;
    
    @Operation(summary = "Create a new SCIM user", description = "Creates a new user resource following SCIM 2.0 schema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - userName is required")
    })
    @PostMapping
    public ResponseEntity<ScimUser> createUser(@RequestBody ScimUser user) {
        ScimUser created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Operation(summary = "Get user by ID", description = "Retrieves a single user resource by their unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ScimUser> getUser(
            @Parameter(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        ScimUser user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "List or search users", description = "Returns a list of users with pagination, filtering, and sorting support")
    @GetMapping
    public ResponseEntity<ScimListResponse<ScimUser>> listUsers(
            @Parameter(description = "SCIM filter expression (e.g., userName eq \"john\")")
            @RequestParam(value = "filter", required = false) String filter,
            @Parameter(description = "Attribute to sort by")
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "Sort order: ascending or descending")
            @RequestParam(value = "sortOrder", defaultValue = "ascending") String sortOrder,
            @Parameter(description = "1-based index of the first result")
            @RequestParam(value = "startIndex", defaultValue = "1") int startIndex,
            @Parameter(description = "Number of results per page")
            @RequestParam(value = "count", defaultValue = "20") int count) {
        
        ScimListResponse<ScimUser> response = userService.listUsers(
            startIndex, count, filter, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update user (PUT)", description = "Replaces the entire user resource with the provided data")
    @PutMapping("/{id}")
    public ResponseEntity<ScimUser> updateUser(
            @Parameter(description = "Unique identifier of the user")
            @PathVariable String id, 
            @RequestBody ScimUser user) {
        ScimUser updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }
    
    @Operation(summary = "Partially update user (PATCH)", description = "Applies partial modifications to a user resource using SCIM patch operations")
    @PatchMapping("/{id}")
    public ResponseEntity<ScimUser> patchUser(
            @Parameter(description = "Unique identifier of the user")
            @PathVariable String id, 
            @RequestBody ScimPatchRequest patchRequest) {
        ScimUser updated = userService.patchUser(id, patchRequest);
        return ResponseEntity.ok(updated);
    }
    
    @Operation(summary = "Delete user", description = "Permanently removes a user resource")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "Unique identifier of the user")
            @PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
