package com.scim_gateway.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = "com.scim_gateway.repository.mongo")
public class MongoDBConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    
    @Value("${spring.data.mongodb.database:scimdb}")
    private String databaseName;
    
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
    
    @Bean
    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
    
    @Override
    protected Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.scim_gateway.model.mongo");
    }
}
