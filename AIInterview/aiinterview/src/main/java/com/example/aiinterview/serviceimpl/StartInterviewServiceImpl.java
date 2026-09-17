package com.example.aiinterview.serviceimpl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.aiinterview.constant.CONSTANT;
import com.example.aiinterview.dto.TokenStatusExpiryDTO;
import com.example.aiinterview.dto.UserAIChatRequestDTO;
import com.example.aiinterview.dto.UserIdAppliedJobId;
import com.example.aiinterview.repository.SaveQuestionRepository;
import com.example.aiinterview.repository.StartInterviewRepository;
import com.example.aiinterview.repository.ValidateLinkRepository;
import com.example.aiinterview.service.InterviewSessionStore;
import com.example.aiinterview.service.StartInterviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j 
@Service
@RequiredArgsConstructor
public class StartInterviewServiceImpl implements StartInterviewService{
  private final PubliserService publiserService;
  private final ValidateLinkRepository validateLinkRepository;
  private final StartInterviewRepository startInterviewRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final InterviewSessionStore interviewSessionStore;
  private final SaveQuestionRepository saveQuestionRepository;
  private final CONSTANT constant;
  private final PubSubTemplate pubSubTemplate;

  // startInterview result codes: 1 = started, 2 = link already used, 3 = link expired, 0 = invalid/other
  @Override
  public Integer startInterview(String token) throws IOException{
    TokenStatusExpiryDTO tokenStatusExpiryDTO;
    try {
      tokenStatusExpiryDTO = validateLinkRepository.fetchStatusAndExpiryDate(token);
    } catch (EmptyResultDataAccessException e) {
      return 0;
    }

    if (tokenStatusExpiryDTO.getStatus().equals(constant.USED)){
      return 2;
    }

    if (!tokenStatusExpiryDTO.getExpiryDate().isAfter(LocalDateTime.now())){
      return 3;
    }

    if (tokenStatusExpiryDTO.getStatus().equals(constant.SUCCESSS)){
      // Link is valid and user is clicking the start button for the first time
      // Update the user status
      if (startInterviewRepository.startInterview(token)==1){
        return 1;
      }
    }

    return 0;
  }
  @Override
  public List<String> fetchInterviewQuestions(String token) {
     UserIdAppliedJobId userIdAppliedJobId = startInterviewRepository.fetchUserIdAndAppliedJobId(token);

     return startInterviewRepository.fetchUserInterviewQuestions(userIdAppliedJobId.getUserId(), userIdAppliedJobId.getAppliedJobId());

  }
  @Override
  public void initiatigInterview(UserAIChatRequestDTO userAIChatRequestDTO, Principal principal) {
    // principal.getName() = the interview token, resolved from the "userId"
    // query param on the WebSocket handshake (see WebSocketConfiguration)
    String token = principal.getName();
    UserAIChatRequestDTO userResponse = new UserAIChatRequestDTO();
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    if (!interviewSessionStore.exists(token)) {
      // First message on this token: resolve the real user/job ids once and kick off the interview
      UserIdAppliedJobId userIdAppliedJobId = startInterviewRepository.fetchUserIdAndAppliedJobId(token);
      List<String> questions = startInterviewRepository.fetchUserInterviewQuestions(userIdAppliedJobId.getUserId(), userIdAppliedJobId.getAppliedJobId());
      String jobDescription = startInterviewRepository.fetchJobDescription(userIdAppliedJobId.getAppliedJobId());
      log.info("Job Description is :::::: {}",jobDescription);
      userAIChatRequestDTO.setJobDescription(jobDescription);
      InterviewSessionStore.Session session = interviewSessionStore.start(token, userIdAppliedJobId.getUserId(), userIdAppliedJobId.getAppliedJobId(), questions);
      sendQuestion(token, session.getUserId(), session.currentQuestion());
      return;
    }

    // Any later message on this token is the candidate's answer to the current question
    InterviewSessionStore.Session session = interviewSessionStore.get(token);
    // TODO: persist/evaluate userAIChatRequestDTO.getContent() as the answer to session.currentQuestion()
    log.info("Candiate answer is :::::: {}",userAIChatRequestDTO.getContent());
    log.info("Candidate user id is :::::: {}",session.getUserId());
    log.info("Candidate applied job id is ::::::: {}",session.getAppliedJobId());
    log.info("Job Description according to job id is ::::::::: {}",userAIChatRequestDTO.getJobDescription());

    // Save all the above fields which we are logging into the database
    Integer rowsInserted = saveQuestionRepository.saveUserResponse(session.getUserId(), session.getAppliedJobId(), session.currentQuestion(),userAIChatRequestDTO.getContent(), constant.PENDING);

    session.advance();

    if (rowsInserted != null && rowsInserted == 1) {
      // Send the candidate answer to the Evalutation Service
      byte [] jsonBytes;
      try{
        userResponse.setContent(userAIChatRequestDTO.getContent());
        userResponse.setQuestion(session.currentQuestion());
        userResponse.setUserId(session.getUserId());
        userResponse.setUserIdAppledJobId(session.getAppliedJobId());
        userResponse.setLocalDateTime(LocalDateTime.now());

        jsonBytes = objectMapper.writeValueAsBytes(userResponse);
      }
      catch(JsonProcessingException e){
         throw new UncheckedIOException("Failed to serialize match payload for pub-sub", e);
      }

      ByteString data = ByteString.copyFrom(jsonBytes);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
        .setData(data)
        .build();

      publiserService.sendMessageToInterviewEvaluationTopic(pubsubMessage);
      // Flip the row(s) for this user+job so tomorrow's cleanup scheduler can pick them up
      saveQuestionRepository.updateUserQuestionResponseStatus(constant.SUCCESSS, session.getUserId(), session.getAppliedJobId());
    } else {
      log.error("Insertion failed in the table user_question_response for userId={}, appliedJobId={}", session.getUserId(), session.getAppliedJobId());
    }

    if (session.hasNext()) {
      sendQuestion(token, session.getUserId(), session.currentQuestion());
    } else {
      interviewSessionStore.end(token);
      sendMessage(token, session.getUserId(), "Interview complete. Thank you.");
    }
  }

  private void sendQuestion(String token, Integer userId, String question) {
    sendMessage(token, userId, question);
  }

  private void sendMessage(String token, Integer userId, String content) {
    UserAIChatRequestDTO message = new UserAIChatRequestDTO();
    message.setUserId(userId);
    message.setContent(content);
    message.setLocalDateTime(LocalDateTime.now());
    // "token" (not userId) is the STOMP principal name resolved at handshake -
    // that's what convertAndSendToUser must route on, regardless of what the
    // DTO body itself carries
    simpMessagingTemplate.convertAndSendToUser(token, "/queue/messages", message);
  }
  
}
