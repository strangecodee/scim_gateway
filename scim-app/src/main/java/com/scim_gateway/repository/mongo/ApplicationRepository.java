package com.scim_gateway.repository.mongo;

import com.scim_gateway.model.mongo.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ApplicationRepository extends MongoRepository<Application, String> {
    List<Application> findByEnabledTrue();
    List<Application> findByAutoProvisionTrueAndEnabledTrue();
}
