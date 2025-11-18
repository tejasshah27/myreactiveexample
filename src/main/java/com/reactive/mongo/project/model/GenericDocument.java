package com.reactive.mongo.project.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Document(collection = "UserData")
public class GenericDocument {

    @Id
    private String id;

    private String type;  // "user" or "order"

    private Map<String, Object> attributeMap;

    public GenericDocument() {}

    public GenericDocument(String id, String type, Map<String, Object> attributeMap) {
        this.id = id;
        this.type = type;
        this.attributeMap = attributeMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }
}