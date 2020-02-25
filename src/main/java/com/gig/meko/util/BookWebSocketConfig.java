package com.gig.meko.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class StocktickerWebSocketConfig implements WebSocketConfigurer {

    private final StockTickerGraphqlPublisher stockTickerGraphqlPublisher;

    @Autowired
    public StocktickerWebSocketConfig(StockTickerGraphqlPublisher stockTickerGraphqlPublisher) {
        this.stockTickerGraphqlPublisher = stockTickerGraphqlPublisher;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new StockTickerWebSocketHandler(stockTickerGraphqlPublisher), "/stockticker").setAllowedOrigins("*");
    }
}

