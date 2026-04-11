package com.scim_gateway.repository.mongo;

import com.scim_gateway.model.mongo.ProvisioningJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProvisioningJobRepository extends MongoRepository<ProvisioningJob, String> {
    List<ProvisioningJob> findByUserId(String userId);
    List<ProvisioningJob> findByApplicationId(String applicationId);
    List<ProvisioningJob> findByStatus(ProvisioningJob.Status status);
    List<ProvisioningJob> findByStatusAndAttemptsLessThan(ProvisioningJob.Status status, int maxAttempts);
}
