package com.scim_gateway.controller;

import com.scim_gateway.model.mongo.MongoUser;
import com.scim_gateway.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping
    public MongoUser create(@RequestBody MongoUser user) {
        return service.createUser(user);
    }
}
