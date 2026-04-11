package com.scim_gateway.adapter;

import com.scim_gateway.model.mongo.MongoUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("dev")
public class InMemoryUserAdapter implements UserAdapter {
    
    private static final Map<String, MongoUser> userStore = new HashMap<>();
    private static int idCounter = 1;

    @Override
    public synchronized MongoUser save(MongoUser user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId("user-" + (idCounter++));
        }
        userStore.put(user.getId(), user);
        return user;
    }
    
    @Override
    public Optional<MongoUser> findById(String id) {
        return Optional.ofNullable(userStore.get(id));
    }
    
    @Override
    public List<MongoUser> findAll() {
        return new ArrayList<>(userStore.values());
    }
    
    @Override
    public synchronized void deleteById(String id) {
        userStore.remove(id);
    }
    
    @Override
    public long count() {
        return userStore.size();
    }
    
    public static Map<String, MongoUser> getUserStore() {
        return userStore;
    }
}
