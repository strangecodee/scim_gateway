package com.scim_gateway.adapter;

import com.scim_gateway.model.sql.SqlUser;
import com.scim_gateway.repository.sql.SqlUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class SqlUserAdapter implements UserAdapter {

    @Autowired
    private SqlUserRepository sqlUserRepo;

    public Object save(Object user) {
        return sqlUserRepo.save((SqlUser) user);
    }
}
