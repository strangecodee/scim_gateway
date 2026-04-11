package com.scim_gateway.service;

import com.scim_gateway.adapter.UserAdapter;
import com.scim_gateway.model.mongo.MongoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserAdapter userAdapter;

    public MongoUser createUser(MongoUser user){
        return userAdapter.save(user);
    }
}
