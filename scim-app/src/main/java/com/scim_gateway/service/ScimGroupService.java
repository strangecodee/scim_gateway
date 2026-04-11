package com.scim_gateway.service;

import com.scim_gateway.model.scim.ScimGroup;
import com.scim_gateway.model.scim.ScimListResponse;
import com.scim_gateway.model.scim.ScimPatchRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ScimGroupService {
    
    // Simple in-memory storage for groups (can be moved to adapter pattern later)
    private static final ConcurrentHashMap<String, ScimGroup> groupStore = new ConcurrentHashMap<>();
    
    public ScimGroup createGroup(ScimGroup group) {
        if (group.getDisplayName() == null || group.getDisplayName().trim().isEmpty()) {
            throw new RuntimeException("displayName is required");
        }
        
        if (group.getId() == null || group.getId().isEmpty()) {
            group.setId(UUID.randomUUID().toString());
        }
        
        String now = Instant.now().toString();
        ScimGroup.Meta meta = new ScimGroup.Meta();
        meta.setResourceType("Group");
        meta.setCreated(now);
        meta.setLastModified(now);
        meta.setLocation("/scim/v2/Groups/" + group.getId());
        group.setMeta(meta);
        
        groupStore.put(group.getId(), group);
        return group;
    }
    
    public ScimGroup getGroup(String id) {
        ScimGroup group = groupStore.get(id);
        if (group == null) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        return group;
    }
    
    public ScimListResponse<ScimGroup> listGroups(int startIndex, int count, String filter, String sortBy, String sortOrder) {
        List<ScimGroup> allGroups = new ArrayList<>(groupStore.values());
        
        // Simple filter on displayName
        if (filter != null && filter.contains("displayName")) {
            String value = filter.replaceAll(".*displayName\\s+eq\\s+\"([^\"]+)\".*", "$1");
            if (!filter.equals(value)) {
                allGroups = allGroups.stream()
                    .filter(g -> g.getDisplayName() != null && g.getDisplayName().equals(value))
                    .collect(Collectors.toList());
            }
        }
        
        int totalResults = allGroups.size();
        
        // Pagination
        int fromIndex = Math.max(0, startIndex - 1);
        int toIndex = Math.min(fromIndex + count, allGroups.size());
        
        List<ScimGroup> paginated = fromIndex < allGroups.size() 
            ? allGroups.subList(fromIndex, toIndex) 
            : new ArrayList<>();
        
        ScimListResponse<ScimGroup> response = new ScimListResponse<>();
        response.setTotalResults(totalResults);
        response.setStartIndex(startIndex);
        response.setItemsPerPage(paginated.size());
        response.setResources(paginated);
        
        return response;
    }
    
    public ScimGroup updateGroup(String id, ScimGroup group) {
        if (!groupStore.containsKey(id)) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        
        group.setId(id);
        group.getMeta().setLastModified(Instant.now().toString());
        
        groupStore.put(id, group);
        return group;
    }
    
    public ScimGroup patchGroup(String id, ScimPatchRequest patchRequest) {
        ScimGroup group = getGroup(id);
        
        for (ScimPatchRequest.Operation op : patchRequest.getOperations()) {
            if ("add".equalsIgnoreCase(op.getOp()) || "replace".equalsIgnoreCase(op.getOp())) {
                if (op.getPath() != null && op.getPath().toLowerCase().contains("members")) {
                    if (op.getValue() instanceof List) {
                        List<ScimGroup.Member> newMembers = (List<ScimGroup.Member>) op.getValue();
                        if (group.getMembers() == null) {
                            group.setMembers(newMembers);
                        } else {
                            group.getMembers().addAll(newMembers);
                        }
                    }
                }
            }
        }
        
        group.getMeta().setLastModified(Instant.now().toString());
        groupStore.put(id, group);
        return group;
    }
    
    public void deleteGroup(String id) {
        if (!groupStore.containsKey(id)) {
            throw new RuntimeException("Group not found with id: " + id);
        }
        groupStore.remove(id);
    }
}
