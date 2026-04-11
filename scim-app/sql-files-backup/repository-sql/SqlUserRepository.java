package com.scim_gateway.repository.sql;

import com.scim_gateway.model.sql.SqlUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SqlUserRepository extends JpaRepository<SqlUser, Long> {

}
