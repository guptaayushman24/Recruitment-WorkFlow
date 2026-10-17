package com.example.aiinterview.service;

import org.springframework.stereotype.Service;

import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO;
import com.example.aiinterview.dto.InterviewQuestionsRecord;
import com.example.aiinterview.repository.SaveQuestionRepository;
import com.example.aiinterview.serviceimpl.LinkActivationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
  private final ObjectMapper objectMapper;
  private final Set<String> inFlight;
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

      // Ack immediately after parsing, before the slow AI call - the
      // subscription's ackDeadlineSeconds (20s) is shorter than question
      // generation can take, so waiting until after it was causing the
      // message to be redelivered mid-processing and race with itself.
      basicAcknowledgeablePubsubMessage.ack();

      createQuestionsAndLink(extractionResumeJobDescriptionDTO);
    }
    catch (Exception e){
      log.error("Something went wrong processing resume-job-description-detail message", e);
    }
  }

  // userId/appliedJobId pairs currently being processed, so the retry scheduler can't start a
  // second run for a candidate whose original Pub/Sub message is still mid-LLM-call
  

  /**
   * Generates the questions, saves them and creates + publishes the interview link. Both the Pub/Sub
   * listener and InterviewRetryScheduler call this. Throws if the LLM or DB fails, so the caller decides
   * what to do. Returns false when the same candidate is already being processed.
   */
  public boolean createQuestionsAndLink(ExtractionResumeJobDescriptionDTO extractionResumeJobDescriptionDTO){
    Integer userId = extractionResumeJobDescriptionDTO.getUserDetail().getUserId();
    Integer appliedJobId = extractionResumeJobDescriptionDTO.getUserDetail().getAppliedJobId();
    String key = userId + "/" + appliedJobId;

    if (!inFlight.add(key)){
      log.info("Interview for {} is already being processed, skipping", key);
      return false;
    }

    try{
      InterviewQuestionsRecord interviewQuestions =
          createInterviewService.createInterviewQuestions(extractionResumeJobDescriptionDTO);

      log.info("Generated interview questions :::: {}", interviewQuestions.getQuestions());

      Integer isInterviewQuestionGenerated = saveQuestionRepository.generateQuestions(
          interviewQuestions.getUserId(),
          interviewQuestions.getAppliedJobId(),
          interviewQuestions.getQuestions());

      if (isInterviewQuestionGenerated==1){
        // in token send the job_id also to correctly give the interview questions
        String userEmailAddress = saveQuestionRepository.userEmailAddress(interviewQuestions.getUserId());
        linkActivationServiceImpl.createAndSendSecureLink(interviewQuestions.getUserId(),interviewQuestions.getAppliedJobId(),userEmailAddress);
      }
      return true;
    }
    finally{
      inFlight.remove(key);
    }
  }
}
