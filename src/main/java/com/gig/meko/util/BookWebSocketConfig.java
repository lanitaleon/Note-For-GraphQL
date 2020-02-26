package com.gig.meko.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class BookWebSocketConfig implements WebSocketConfigurer {

    private final GraphQLProvider graphQLProvider;

    @Autowired
    public BookWebSocketConfig(GraphQLProvider graphQLProvider) {
        this.graphQLProvider = graphQLProvider;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new BookWebSocketHandler(graphQLProvider), "/publishBook").setAllowedOrigins("*");
    }
}

