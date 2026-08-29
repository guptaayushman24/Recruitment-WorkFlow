package com.example.extractionservice.service;

import org.springframework.stereotype.Service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.pubsub.v1.PubsubMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor

public class PublisherService {
  private final PubSubTemplate pubSubTemplate;
   public void sendMessageToExtractionTopic(PubsubMessage ExtractionResumeJobDescriptionDTO){
        pubSubTemplate.publish("resume-job-description-detail",ExtractionResumeJobDescriptionDTO)
                .whenComplete((messageId, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to publish message to topic 'resume-job-description-detail'", throwable);
                    } else {
                        log.info("Published message {} to topic 'resume-job-description-detail'", messageId);
                    }
                });
    }

    public void sendMessageToEmailTopic (PubsubMessage EmailDTO){
        pubSubTemplate.publish("user-detail-email", EmailDTO)
        .whenComplete((messageId,throwable)->{
            if (throwable!=null){
                log.error("Failed to publish message to topic 'user-detail-email'", throwable);
            }
            else{
                  log.info("Published message {} to topic 'user-detail-email'", messageId);
            }
        });
    }
}
