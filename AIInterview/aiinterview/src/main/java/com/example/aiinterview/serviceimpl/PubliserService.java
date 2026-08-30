package com.example.aiinterview.serviceimpl;
import org.springframework.stereotype.Service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PubliserService {
  private final PubSubTemplate pubSubTemplate;

  public void sendMessageToAIInterviewTopic (PubsubMessage SecureLinkToken){
    pubSubTemplate.publish("user-interview-activation",SecureLinkToken)
                .whenComplete((messageId, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish message to topic 'resume-job-description-detail'", throwable);
                    } else {
                        log.info("Published message {} to topic 'resume-job-description-detail'", messageId);
                    }
                });
  }
}