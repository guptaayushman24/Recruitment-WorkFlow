package com.example.aiinterview.service;

import org.springframework.stereotype.Service;

import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO;
import com.example.aiinterview.dto.InterviewQuestionsRecord;
import com.example.aiinterview.repository.SaveQuestionRepository;
import com.example.aiinterview.serviceimpl.LinkActivationServiceImpl;
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
  private final LinkActivationServiceImpl linkActivationServiceImpl;
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

      Integer isInterviewQuestionGenerated = saveQuestionRepository.generateQuestions(
          interviewQuestions.getUserId(),
          interviewQuestions.getAppliedJobId(),
          interviewQuestions.getQuestions());

     if (isInterviewQuestionGenerated==1){
        // Call the method to generate the link and save in the database
        // send the userid in the below function 
        // in token send the job_id also to correctly give the interview questions
        String userEmailAddress = saveQuestionRepository.userEmailAddress(interviewQuestions.getUserId());
        linkActivationServiceImpl.createAndSendSecureLink(interviewQuestions.getUserId(),interviewQuestions.getAppliedJobId(),userEmailAddress);
     }
    }
    catch (Exception e){
      log.error(e.getMessage(),"Something went wrong");
    }
  }
}
