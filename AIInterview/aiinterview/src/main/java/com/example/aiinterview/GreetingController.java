package com.example.aiinterview;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.example.Greeting;
import com.example.HelloMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GreetingController {
 private final SimpMessagingTemplate messagingTemplate;
 @MessageMapping("/chat.send")
    @SendTo("/topic/messages") // these send the message to the broker
    public ChatMessage send(ChatMessage message) {
      log.info("Inside the Chat Message function ::::: {}",message.getContent());
        return message;   // returned object is pushed to all subscribers
    }

    @MessageMapping("/private-message")
    public void recievePrivateMessage(@Payload ChatMessagePrivate chatMessagePrivate){
       String topic = privateTopicFor(chatMessagePrivate.getSenderId(), chatMessagePrivate.getReceiverId());
       messagingTemplate.convertAndSend("/topic/private/" + topic, chatMessagePrivate);
    }

    // deterministic per-pair topic name - same value regardless of who is sender/receiver
    private String privateTopicFor(String userA, String userB) {
      return userA.compareTo(userB) < 0 ? userA + "_" + userB : userB + "_" + userA;
    }
  }
