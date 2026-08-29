package com.example.mailservice.serviceimpl;

import org.springframework.stereotype.Service;

import com.example.mailservice.dto.EmailDTO;
import com.example.mailservice.service.EmailService;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
@Service
@RequiredArgsConstructor
@Slf4j
public class SubScriberServiceImpl {
  private final PubSubTemplate pubSubTemplate;
  private final EmailService emailService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @PostConstruct
  public void startSubscriber(){
    pubSubTemplate.subscribe("user-detail-email-sub", this::userDetail);
  }

  private void userDetail(BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage){
    try{
      String payload = basicAcknowledgeablePubsubMessage.getPubsubMessage().getData().toStringUtf8();

      String type = basicAcknowledgeablePubsubMessage.getPubsubMessage().getAttributesOrDefault("type", "unknown");

      log.info("Received message on user-detail-email-sub: payload={}, type={}", payload, type);

      EmailDTO emailDTO = objectMapper.readValue(payload, EmailDTO.class);

      // Pass the emailDTO in the email service for sending the email address
      emailService.sendEmailToUser(emailDTO);

    }
    catch(Exception e){
      log.error(e.getMessage(),"Something went wrong");
    }
  }
}