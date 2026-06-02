package com.fadax.migratemongotomaria.repository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class GenericMongoRepository {

    private final MongoTemplate mongoTemplate;

    public GenericMongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> findAll(String collectionName) {
        return mongoTemplate.findAll(Document.class, collectionName);
    }
}
