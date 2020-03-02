package com.gig.meko.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.execution.preparsed.PreparsedDocumentProvider;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.util.function.Function;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

/**
 * schema
 *
 * @author spp
 */
@Component
public class GraphQLProvider {
    private GraphQL graphQL;
    private GraphQLSchema graphQLSchema;

    @Resource
    private GraphQLDataFetchers graphQLDataFetchers;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        Cache<String, PreparsedDocumentEntry> cache = Caffeine.newBuilder().maximumSize(10_000).build();
        PreparsedDocumentProvider preparsedDocumentProvider =
                (ExecutionInput executionInput, Function<ExecutionInput, PreparsedDocumentEntry> function) -> {
                    Function<String, PreparsedDocumentEntry> mapCompute = key -> function.apply(executionInput);
                    return cache.get(executionInput.getQuery(), mapCompute);
                };

        URL url = Resources.getResource("schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema)
                .preparsedDocumentProvider(preparsedDocumentProvider)
                .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy())
                .build();
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                        .dataFetcher("bookById", graphQLDataFetchers.getBookById())
                        .dataFetcher("booksByNameAndAuthor2", graphQLDataFetchers.listBookByNameByAuthor())
                        .dataFetcher("listBook", graphQLDataFetchers.listBook())
                        .dataFetcher("booksByAuthor", graphQLDataFetchers.listBookByAuthor())
                        .dataFetcher("booksByAuthorName", graphQLDataFetchers.listBookByAuthorName())
                        .dataFetcher("booksByNameAndAuthor", graphQLDataFetchers.listBookByNameByAuthor())
                )
                .type(newTypeWiring("Book")
                        .dataFetcher("author", graphQLDataFetchers.getAuthor()))
                .type(newTypeWiring("Mutation")
                        .dataFetcher("addBook", graphQLDataFetchers.addBook()))
                .type(newTypeWiring("Mutation")
                        .dataFetcher("updateBookName", graphQLDataFetchers.updateBookName()))
                .type(newTypeWiring("Subscription")
                        .dataFetcher("publishBook", graphQLDataFetchers.publishBook()))
                .build();
    }

}
