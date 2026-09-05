package com.example.aiinterview;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer{

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry){
    // The single HTTP handshake happens here, then upgrades to WebSocket.
    // There's no Spring Security in this project, so nothing assigns a
    // Principal to the session by default - and convertAndSendToUser(...)
    // (used to push a question to one candidate's /user/queue/messages)
    // silently drops the message without one. This handler resolves the
    // Principal from a "userId" query param on connect, e.g. /ws?userId=123
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")   // lock this down in production
            .setHandshakeHandler(new DefaultHandshakeHandler() {
              @Override
              protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                String userId = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("userId");
                return userId == null ? null : (Principal) () -> userId;
              }
            })
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