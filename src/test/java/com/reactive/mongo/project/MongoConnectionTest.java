/*
package com.reactive.mongo.project;


import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MongoConnectionTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void pingMongo() {
        Document result = mongoTemplate.getDb().runCommand(new Document("ping", 1));
        Number ok = (Number) result.get("ok");
        assertNotNull(ok, "No 'ok' field returned from ping");
        assertEquals(1, ok.intValue(), "MongoDB ping did not return ok=1");
    }
}
*/
