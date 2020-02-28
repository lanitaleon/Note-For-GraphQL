package com.gig.meko.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Graphql clients can send GET or POST HTTP requests.  The spec does not make an explicit
 * distinction.  So you may need to handle both.  The following was tested using
 * a graphiql client tool found here : https://github.com/skevy/graphiql-app
 * <p>
 * You should consider bundling graphiql in your application
 * <p>
 * https://github.com/graphql/graphiql
 * <p>
 * This outlines more information on how to handle parameters over http
 * <p>
 * http://graphql.org/learn/serving-over-http/
 */
public class QueryParameters {

    private String query;
    private String operationName;
    private Map<String, Object> variables = Collections.emptyMap();

    public String getQuery() {
        return query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public static QueryParameters from(String queryMessage) throws JsonProcessingException {
        QueryParameters parameters = new QueryParameters();
        Map<String, Object> json = new ObjectMapper().readValue(queryMessage, new TypeReference<Map<String, Object>>() {
        });
        parameters.query = (String) json.get("query");
        parameters.operationName = (String) json.get("operationName");
        parameters.variables = getVariables(json.get("variables"));
        return parameters;
    }


    private static Map<String, Object> getVariables(Object variables) throws JsonProcessingException {
        if (variables instanceof Map) {
            Map<?, ?> inputVars = (Map) variables;
            Map<String, Object> vars = new HashMap<>();
            inputVars.forEach((k, v) -> vars.put(String.valueOf(k), v));
            return vars;
        }
        return new ObjectMapper().readValue(String.valueOf(variables), new TypeReference<Map<String, Object>>() {
        });
    }

}
