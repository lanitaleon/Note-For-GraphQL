package com.gig.meko.util;

import com.gig.meko.service.AccessService;
import graphql.GraphQLContext;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;

import java.util.Collections;
import java.util.List;

/**
 * @author spp
 */
public class CustomFieldVisibility implements GraphqlFieldVisibility {

    private final AccessService accessService;

    CustomFieldVisibility(AccessService accessService) {
        this.accessService = accessService;
    }

    @Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
        if ("AdminType".equals(fieldsContainer.getName())) {
            if (!accessService.isAdminUser()) {
                return Collections.emptyList();
            }
        }
        return fieldsContainer.getFieldDefinitions();
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
        if ("AdminType".equals(fieldsContainer.getName())) {
            if (!accessService.isAdminUser()) {
                return null;
            }
        }
        return fieldsContainer.getFieldDefinition(fieldName);
    }
}
