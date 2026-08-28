package com.example.aiinterview.service;

import org.springframework.stereotype.Service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubScriberService {
  private final PubSubTemplate pubSubTemplate;

  @PostConstruct
  public void startSubscriber(){
        pubSubTemplate.subscribe("resume-job-description-detail-sub",this::userResumeJobExtraction);
  }

  private void userResumeJobExtraction (BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage){

    try{
      String payload = basicAcknowledgeablePubsubMessage.getPubsubMessage().getData().toStringUtf8();
      String type = basicAcknowledgeablePubsubMessage.getPubsubMessage().getAttributesOrDefault("type", "unknown");

      log.info("Received message on test-sub: payload={}, type={}", payload, type);
            basicAcknowledgeablePubsubMessage.ack();
    }
    catch (Exception e){
      log.error(e.getMessage(),"Something went wrong");
    }

  }
}
