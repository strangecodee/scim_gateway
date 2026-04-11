package com.scim_gateway.adapter;

import com.scim_gateway.model.mongo.MongoUser;

import java.util.List;
import java.util.Optional;

public interface UserAdapter {
    MongoUser save(MongoUser user);
    Optional<MongoUser> findById(String id);
    List<MongoUser> findAll();
    void deleteById(String id);
    long count();
}
