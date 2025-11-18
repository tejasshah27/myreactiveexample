package com.reactive.mongo.project.controller;

import com.reactive.mongo.project.model.GenericDocument;
import com.reactive.mongo.project.util.ProjectionUtil;
import graphql.Mutable;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DocumentController {

    private final ReactiveMongoTemplate mongoTemplate;

    public DocumentController(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @QueryMapping
    public Flux<Map<String, Object>> users(DataFetchingEnvironment env) {
        Query query = Query.query(Criteria.where("type").is("user"));
        ProjectionUtil.applyProjection(query, env);

        return mongoTemplate.find(query, GenericDocument.class)
                .map(this::flattenDocument);
    }

    @QueryMapping
    public Flux<Map<String, Object>> orders(DataFetchingEnvironment env) {
        Query query = Query.query(Criteria.where("type").is("order"));
        ProjectionUtil.applyProjection(query, env);

        return mongoTemplate.find(query, GenericDocument.class)
                .map(this::flattenDocument);
    }

    @QueryMapping
    public Mono<Map<String, Object>> documentById(@Argument String id) {
        return mongoTemplate.findById(id, GenericDocument.class)
                .map(doc -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", doc.getId());
                    result.put("type", doc.getType());
                    result.put("data", doc.getAttributeMap());
                    return result;
                });
    }

    @QueryMapping
    public Mono<Map<String,Object>> getUserById(@Argument Integer identify) {
        Query query = Query.query(Criteria.where("type").is("user").and("attributeMap.identify").is(identify));
        return mongoTemplate.findOne(query, GenericDocument.class)
                .map(doc -> {
                    Map<String, Object> userMap = new HashMap<>();
                    if (doc.getAttributeMap() != null) {
                        doc.getAttributeMap().forEach((key, value) -> {
                            if (value instanceof String) {
                                userMap.put(key, (String) value);
                            }
                            if (value instanceof Integer) {
                                userMap.put(key, (Integer) value);
                            }
                        });
                    }
                    return userMap;
                });
    }

    @MutationMapping
    public Mono<Map<String,Object>> updateUserNameByIdentify(@Argument Integer identify, @Argument String newName) {
        Query query = Query.query(Criteria.where("type").is("user").and("attributeMap.identify").is(identify));
        Update update = new Update().set("attributeMap.name", newName);
        return mongoTemplate.updateMulti(query, update, GenericDocument.class)
                .flatMap(updateResult -> {
                    if(updateResult.getModifiedCount() == 0)
                        return Mono.error(new RuntimeException("No user found with identify: " + identify));
                    return mongoTemplate.findOne(query, GenericDocument.class);

                })
                .map(this::flattenDocument);
    }

    private Map<String, Object> flattenDocument(GenericDocument doc) {
        Map<String, Object> flattened = new HashMap<>();
        flattened.put("id", doc.getId());

        if (doc.getAttributeMap() != null) {
            flattened.putAll(doc.getAttributeMap());
        }

        return flattened;
    }
}