package com.scim_gateway.adapter;

import com.scim_gateway.model.mongo.MongoUser;
import com.scim_gateway.repository.mongo.MongoUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Profile("mongo")
public class MongoUserAdapter implements UserAdapter {
    
    @Autowired
    private MongoUserRepository mongoUserRepo;
    
    @Override
    public MongoUser save(MongoUser user) {
        return mongoUserRepo.save(user);
    }
    
    @Override
    public Optional<MongoUser> findById(String id) {
        return mongoUserRepo.findById(id);
    }
    
    @Override
    public List<MongoUser> findAll() {
        return mongoUserRepo.findAll();
    }
    
    @Override
    public void deleteById(String id) {
        mongoUserRepo.deleteById(id);
    }
    
    @Override
    public long count() {
        return mongoUserRepo.count();
    }
}
