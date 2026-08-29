package com.example.aiinterview.service;

import org.springframework.stereotype.Service;

import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO;
import com.example.aiinterview.dto.InterviewQuestionsRecord;
import com.example.aiinterview.repository.SaveQuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final CreateInterviewService createInterviewService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final SaveQuestionRepository saveQuestionRepository;
  @PostConstruct
  public void startSubscriber(){
        pubSubTemplate.subscribe("resume-job-description-detail-sub",this::userResumeJobExtraction);
  }

  private void userResumeJobExtraction (BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage){

    try{
      String payload = basicAcknowledgeablePubsubMessage.getPubsubMessage().getData().toStringUtf8();
      String type = basicAcknowledgeablePubsubMessage.getPubsubMessage().getAttributesOrDefault("type", "unknown");

      log.info("Received message on resume-job-description-detail: payload={}, type={}", payload, type);

      ExtractionResumeJobDescriptionDTO extractionResumeJobDescriptionDTO =
          objectMapper.readValue(payload, ExtractionResumeJobDescriptionDTO.class);

      InterviewQuestionsRecord interviewQuestions =
          createInterviewService.createInterviewQuestions(extractionResumeJobDescriptionDTO);

      log.info("Generated interview questions :::: {}", interviewQuestions.getQuestions());

      basicAcknowledgeablePubsubMessage.ack();

      saveQuestionRepository.generateQuestions(
          interviewQuestions.getUserId(),
          interviewQuestions.getAppliedJobId(),
          interviewQuestions.getQuestions());
    }
    catch (Exception e){
      log.error(e.getMessage(),"Something went wrong");
    }
  }
}
