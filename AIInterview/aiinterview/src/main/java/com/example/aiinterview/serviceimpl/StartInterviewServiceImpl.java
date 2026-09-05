package com.example.aiinterview.serviceimpl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.aiinterview.constant.CONSTANT;
import com.example.aiinterview.dto.TokenStatusExpiryDTO;
import com.example.aiinterview.dto.UserAIChatRequestDTO;
import com.example.aiinterview.dto.UserIdAppliedJobId;
import com.example.aiinterview.repository.StartInterviewRepository;
import com.example.aiinterview.repository.ValidateLinkRepository;
import com.example.aiinterview.service.InterviewSessionStore;
import com.example.aiinterview.service.StartInterviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j 
@Service
@RequiredArgsConstructor
public class StartInterviewServiceImpl implements StartInterviewService{
  private final ValidateLinkRepository validateLinkRepository;
  private final StartInterviewRepository startInterviewRepository;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final InterviewSessionStore interviewSessionStore;
  private final CONSTANT constant;
  @Override
  public Integer startInterview(String token) {
    TokenStatusExpiryDTO tokenStatusExpiryDTO =  validateLinkRepository.fetchStatusAndExpiryDate(token);
    if (tokenStatusExpiryDTO.getStatus().equals(constant.SUCCESSS) && tokenStatusExpiryDTO.getExpiryDate().isAfter(LocalDateTime.now())){
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

    if (!interviewSessionStore.exists(token)) {
      // First message on this token: kick off the interview
      InterviewSessionStore.Session session = interviewSessionStore.start(token, fetchInterviewQuestions(token));
      sendQuestion(token, session.currentQuestion());
      return;
    }

    // Any later message on this token is the candidate's answer to the current question
    InterviewSessionStore.Session session = interviewSessionStore.get(token);
    // TODO: persist/evaluate userAIChatRequestDTO.getContent() as the answer to session.currentQuestion()
    log.info("Candiate answer is :::::: {}",userAIChatRequestDTO.getContent());
    session.advance();
    // Send the candidate answer to the Evalutation Service

    if (session.hasNext()) {
      sendQuestion(token, session.currentQuestion());
    } else {
      interviewSessionStore.end(token);
      sendMessage(token, "Interview complete. Thank you.");
    }
  }

  private void sendQuestion(String token, String question) {
    sendMessage(token, question);
  }

  private void sendMessage(String token, String content) {
    UserAIChatRequestDTO message = new UserAIChatRequestDTO();
    message.setUserId(token);
    message.setContent(content);
    message.setLocalDateTime(LocalDateTime.now());
    // deliver ONLY to this candidate's private queue
    simpMessagingTemplate.convertAndSendToUser(token, "/queue/messages", message);
  }
  
}
