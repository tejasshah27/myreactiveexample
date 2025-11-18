package com.reactive.mongo.project.util;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.springframework.data.mongodb.core.query.Query;
import java.util.HashSet;
import java.util.Set;

public class ProjectionUtil {

    public static void applyProjection(Query query, DataFetchingEnvironment env) {
        Set<String> fields = extractFields(env.getSelectionSet());

        if (!fields.isEmpty()) {
            // Always include id and type
            query.fields().include("_id", "type");

            // Add attributeMap fields with projection
            fields.forEach(field -> {
                if (!field.equals("id") && !field.equals("type")) {
                    query.fields().include("attributeMap." + field);
                }
            });
        }
    }

    private static Set<String> extractFields(DataFetchingFieldSelectionSet selectionSet) {
        Set<String> fields = new HashSet<>();

        for (SelectedField field : selectionSet.getImmediateFields()) {
            String fieldName = field.getName();

            if (!fieldName.startsWith("__")) {
                fields.add(fieldName);
            }
        }

        return fields;
    }
}