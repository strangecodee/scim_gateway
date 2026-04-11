package com.scim_gateway.repository.mongo;

import com.scim_gateway.model.mongo.MongoUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoUserRepository extends MongoRepository<MongoUser, String> {

}

