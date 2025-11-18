package com.reactive.mongo.project.controller;

import com.reactive.mongo.project.model.GenericDocument;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stream")
public class StreamController {

    public final Logger logger = org.slf4j.LoggerFactory.getLogger(StreamController.class);

    private final ReactiveMongoTemplate mongoTemplate;

    public StreamController(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Following is working just fine , do not touch !
//    @GetMapping(value = "/users", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<List<Map<String, Object>>> streamUsers() {
//        Query query = Query.query(Criteria.where("type").is("user"));
//        query.fields().include("_id", "type", "attributeMap.name", "attributeMap.email");
//
//        return mongoTemplate.find(query, GenericDocument.class)
//                .onBackpressureBuffer(1500)
//                // Batch 1000 at a time
//
//                .map(this::flattenDocument)
//                .buffer(500);
//                //.doOnNext(doc -> System.out.println("Streaming user: " + doc))
//                //.publishOn(Schedulers.boundedElastic());
//    }

    @GetMapping(value = "/users", produces = {MediaType.TEXT_EVENT_STREAM_VALUE ,MediaType.APPLICATION_NDJSON_VALUE,MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_STREAM_JSON_VALUE})
    public Publisher<?> streamUsers(ServerHttpRequest request) {
        Query query = Query.query(Criteria.where("type").is("user"));
        query.fields().include("_id", "type", "attributeMap.name", "attributeMap.email");

        System.out.println("Requested Media Types: " + request.getHeaders().getAccept());

        if(request.getHeaders().getAccept().stream()
                .anyMatch(a -> a.isCompatibleWith(MediaType.TEXT_EVENT_STREAM))){
            // Stream as NDJSON or SSE
            System.out.println("Streaming as TEXT_EVENT_STREAM_VALUE");
            return mongoTemplate.find(query, GenericDocument.class)
                    .subscribeOn(Schedulers.boundedElastic())
                    .onBackpressureBuffer(1500)
                    .map(this::flattenDocument)
                    .buffer(500)
                    .timestamp()
                    .doOnNext(tsBatch -> {
                        long timestamp = tsBatch.getT1();
                        List<Map<String, Object>> batch = tsBatch.getT2();
                        int batchSize = batch.size();
                        logger.info("streamMetrics = {}   batchSize = {}   timestamp = {}",
                                "userBatchEmission", batchSize, timestamp);
                    });
                    //.map(tsBatch -> tsBatch.getT2());

        }else {
            // Return as a single JSON array
            System.out.println("Returning as APPLICATION_JSON_VALUE");
            return mongoTemplate.find(query, GenericDocument.class)
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(this::flattenDocument)
                    .collectList()
                    .doOnSuccess(list -> logger.info("streamMetrics={} batchSize={}",
                    "userFullEmission", list.size()))
                    .log();
        }
        //.doOnNext(doc -> System.out.println("Streaming user: " + doc))
        //.publishOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> streamOrders() {
        Query query = Query.query(Criteria.where("type").is("order"));
        query.fields().include("_id", "type", "attributeMap.orderId", "attributeMap.amount");

        return mongoTemplate.find(query, GenericDocument.class)
                .buffer(50)
                .flatMap(Flux::fromIterable)
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