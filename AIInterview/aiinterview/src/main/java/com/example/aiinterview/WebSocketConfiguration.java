package com.example.aiinterview;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer{

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry){
    // The single HTTP handshake happens here, then upgrades to WebSocket
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")   // lock this down in production
            .withSockJS();
    //registry.addEndpoint("/ws-raw").setAllowedOriginPatterns("*");
    log.info("Inside the registerStompEndpoints ::::: {}",registry);
  }

  @Override
  public  void configureMessageBroker(MessageBrokerRegistry registry) {
   // Server -> client: destinations clients subscribe to
   // topic is used for the public message and queue is used for private messaging
        registry.enableSimpleBroker("/topic","/queue");
        log.info("Inside the configureMessageBroker :::: {}","Topic API");
        // Client -> server: prefix for @MessageMapping handlers
        registry.setApplicationDestinationPrefixes("/app");
        log.info("Inside the configureMessageBroker :::: {}","App API");

	}
}