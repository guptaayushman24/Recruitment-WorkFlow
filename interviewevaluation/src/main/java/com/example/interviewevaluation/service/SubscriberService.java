package com.example.interviewevaluation.service;

import org.springframework.stereotype.Service;

import com.example.interviewevaluation.dto.UserAIChatRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service 
@RequiredArgsConstructor
@Slf4j 
public class SubscriberService {
  private final PubSubTemplate pubSubTemplate;
  private final InterviewEvaluationService interviewEvaluationService;


  @PostConstruct
  public void startSubsciber(){
     pubSubTemplate.subscribe("user-question-response-sub",this::userInterviewQuestionResponses);
  }

  private void userInterviewQuestionResponses(BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage){
    try{
      ObjectMapper objectMapper = new ObjectMapper();
      String payload = basicAcknowledgeablePubsubMessage.getPubsubMessage().getData().toStringUtf8();
      String type = basicAcknowledgeablePubsubMessage.getPubsubMessage().getAttributesOrDefault("type", "unknown");

      log.info("Received message on user-question-response-sub: payload={}, type={}", payload, type);

      UserAIChatRequestDTO userAIChatRequestDTO = objectMapper.readValue(payload, UserAIChatRequestDTO.class);

      log.info("Candidate answer in interview evaluation ::::: {}",userAIChatRequestDTO.getContent());
      log.info("Candidate question in interview evaluation :::::: {}",userAIChatRequestDTO.getQuestion());
      log.info("User Id in interview evaluation :::: {}",userAIChatRequestDTO.getUserId());
      log.info("Applied Job Id in interview evaluation ::::: {}",userAIChatRequestDTO.getUserIdAppledJobId());
      log.info("Job Description in the interview evaluation ::::::: {}",userAIChatRequestDTO.getJobDescription());
      
      // Pass the question candidate answer for the evaluation and store the score in against the question 
      // reddis
      // AnswerEvaluation candidateScore = interviewEvaluationAssistant.evaluateAnswer(userAIChatRequestDTO.getJobDescription(),userAIChatRequestDTO.getQuestion(), userAIChatRequestDTO.getContent());
      Integer isCandidateScoreStoredInReddis = interviewEvaluationService.getInterviewScore(userAIChatRequestDTO.getJobDescription(), userAIChatRequestDTO.getQuestion(), userAIChatRequestDTO.getContent(),userAIChatRequestDTO.getUserId(),userAIChatRequestDTO.getUserIdAppledJobId());
      
      // If isCandidateScoreStoredInReddis==1 then pass into the service class which will store the data in the table
      if (isCandidateScoreStoredInReddis==1){
        interviewEvaluationService.saveCandidateInterviewScore(userAIChatRequestDTO.getUserId(),userAIChatRequestDTO.getUserIdAppledJobId());
      }
      
    }
    catch (Exception e){
      log.error(e.getMessage(),"Something went wrong");
    }

  }

  // evaluateAnswer(@V(value="jobDescription") String jobDescription, @V(value="question") String question, @V(value="answer") String answer)

}
