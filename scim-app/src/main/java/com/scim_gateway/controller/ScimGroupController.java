package com.scim_gateway.controller;

import com.scim_gateway.model.scim.ScimGroup;
import com.scim_gateway.model.scim.ScimListResponse;
import com.scim_gateway.model.scim.ScimPatchRequest;
import com.scim_gateway.service.ScimGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scim/v2/Groups")
public class ScimGroupController {
    
    @Autowired
    private ScimGroupService groupService;
    
    @PostMapping
    public ResponseEntity<ScimGroup> createGroup(@RequestBody ScimGroup group) {
        ScimGroup created = groupService.createGroup(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ScimGroup> getGroup(@PathVariable String id) {
        ScimGroup group = groupService.getGroup(id);
        return ResponseEntity.ok(group);
    }
    
    @GetMapping
    public ResponseEntity<ScimListResponse<ScimGroup>> listGroups(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "ascending") String sortOrder,
            @RequestParam(value = "startIndex", defaultValue = "1") int startIndex,
            @RequestParam(value = "count", defaultValue = "20") int count) {
        
        ScimListResponse<ScimGroup> response = groupService.listGroups(
            startIndex, count, filter, sortBy, sortOrder
        );
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ScimGroup> updateGroup(@PathVariable String id, @RequestBody ScimGroup group) {
        ScimGroup updated = groupService.updateGroup(id, group);
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ScimGroup> patchGroup(@PathVariable String id, @RequestBody ScimPatchRequest patchRequest) {
        ScimGroup updated = groupService.patchGroup(id, patchRequest);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
