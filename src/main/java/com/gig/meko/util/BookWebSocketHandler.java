package com.gig.meko.util;

//import com.graphql.java.subscription.utill.JsonKit;
//import com.graphql.java.subscription.utill.QueryParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonList;

public class BookWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BookWebSocketHandler.class);

    private final GraphQLProvider graphqlPublisher;
    private final AtomicReference<Subscription> subscriptionRef;

    public BookWebSocketHandler(GraphQLProvider graphqlPublisher) {
        this.graphqlPublisher = graphqlPublisher;
        subscriptionRef = new AtomicReference<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Websocket connection established");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Closing subscription ");
        Subscription subscription = subscriptionRef.get();
        if (subscription != null) {
            subscription.cancel();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws Exception {
        String graphqlQuery = message.getPayload();
        log.info("Websocket said {}", graphqlQuery);

        Instrumentation instrumentation = new ChainedInstrumentation(
                singletonList(new TracingInstrumentation())
        );

        //
        // In order to have subscriptions in graphql-java you MUST use the
        // SubscriptionExecutionStrategy strategy.
        //
        GraphQL graphQL = GraphQL
                .newGraphQL(graphqlPublisher.getGraphQLSchema())
                .instrumentation(instrumentation)
                .build();

        ExecutionResult executionResult = graphQL.execute(graphqlQuery);

        Publisher<ExecutionResult> stockPriceStream = executionResult.getData();

        stockPriceStream.subscribe(new Subscriber<ExecutionResult>() {

            @Override
            public void onSubscribe(Subscription s) {
                subscriptionRef.set(s);
                request(1);
            }

            @Override
            public void onNext(ExecutionResult er) {
                System.out.println("subscribe next");
                log.debug("Sending stick price update");
                try {
                    Object stockPriceUpdate = er.getData();
                    String json = new ObjectMapper().writeValueAsString(stockPriceUpdate);
                    webSocketSession.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                request(1);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Subscription threw an exception", t);
                try {
                    webSocketSession.close();
                } catch (IOException e) {
                    log.error("Unable to close websocket session", e);
                }
            }

            @Override
            public void onComplete() {
                log.info("Subscription complete");
                try {
                    webSocketSession.close();
                } catch (IOException e) {
                    log.error("Unable to close websocket session", e);
                }
            }
        });
    }

    private void request(int n) {
        Subscription subscription = subscriptionRef.get();
        if (subscription != null) {
            subscription.request(n);
        }
    }

}
