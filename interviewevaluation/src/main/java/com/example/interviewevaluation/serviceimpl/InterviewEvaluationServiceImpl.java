package com.example.interviewevaluation.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.interviewevaluation.ai.AnswerEvaluation;
import com.example.interviewevaluation.ai.InterviewEvaluationAssistant;
import com.example.interviewevaluation.constant.Constant;
import com.example.interviewevaluation.dto.SecureLinkToken;
import com.example.interviewevaluation.dto.UserInterviewReport;
import com.example.interviewevaluation.repostiroty.StoreCandidateInterviewScoreRepository;
import com.example.interviewevaluation.service.InterviewEvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewEvaluationServiceImpl implements InterviewEvaluationService {
  private final InterviewEvaluationAssistant interviewEvaluationAssistant;
  private final RedisTemplate<String, Double> redisTemplate;
  private final StoreCandidateInterviewScoreRepository storeCandidateInterviewScoreRepository;
  private final PubSubTemplate pubSubTemplate;
  private final Constant constant;

  // Base URL of the codeeditor frontend (Vite dev server by default)
  @Value("${codeeditor.frontend-url:http://localhost:5173}")
  private String codeEditorFrontendUrl;

  @Override
  public Integer getInterviewScore(String jobDescription, String question, String answer, Integer userId,
      Integer appliedJobId) {
    try {
      AnswerEvaluation candidateAnswer = interviewEvaluationAssistant.evaluateAnswer(jobDescription, question, answer);
      String compositeKey = String.format("userId:%s:appliedJobId:%s", userId, appliedJobId);
      redisTemplate.opsForValue().set(compositeKey, candidateAnswer.getScore());

      log.info("Job Description in interview evaluation service at line number 37 ::::::: {}",jobDescription);

      return 1;
    } catch (Exception e) {
      log.error("Some error is occured in storing the final score in the reddis ::::::: {}",e);
      
      return 0;
    }
  }


  // One transaction so the round-1 score and the round-2 link are saved together or not at all.
  // Exceptions must propagate out of here (the subscriber logs them) or the rollback never happens.
  @Override
  @Transactional
  public void saveCandidateInterviewScore(Integer userId, Integer appliedJobId) {
    String compositeKey = String.format("userId:%s:appliedJobId:%s", userId, appliedJobId);
    Double candidateInterviewScore = redisTemplate.opsForValue().get(compositeKey);

    if (candidateInterviewScore == null) {
      log.warn("No score found in redis for key {}, nothing to save", compositeKey);
      return;
    }

    storeCandidateInterviewScoreRepository.saveCandidateInterviewScore(userId, appliedJobId, candidateInterviewScore, constant.PENDING_STATUS, constant.FIRST_ROUND);

    if (candidateInterviewScore > constant.ROUND_TWO_PASS_SCORE) {
      createRoundTwoLink(userId, appliedJobId);
    }
  }

  private void createRoundTwoLink(Integer userId, Integer appliedJobId) {
    if (storeCandidateInterviewScoreRepository.userLinkExists(userId, appliedJobId, constant.SECOND_ROUND)) {
      log.info("Round {} link already exists for userId={}, appliedJobId={}, skipping", constant.SECOND_ROUND, userId, appliedJobId);
      return;
    }

    String token = UUID.randomUUID().toString();
    String link = codeEditorFrontendUrl + "/?token=" + token;
    // Distinct from round 1's "userId/appliedJobId" so aiinterview's duplicate-link check isn't affected
    String storedInformation = userId + "/" + appliedJobId + "/" + constant.SECOND_ROUND;

    LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(constant.LINK_EXPIRY_MINUTES);
    storeCandidateInterviewScoreRepository.saveUserLink(token, storedInformation, userId, appliedJobId,
        expiryDate, link, constant.PENDING_STATUS, constant.SECOND_ROUND);

    log.info("Round {} link created for userId={}, appliedJobId={}", constant.SECOND_ROUND, userId, appliedJobId);

    SecureLinkToken secureLinkToken = new SecureLinkToken();
    secureLinkToken.setToken(token);
    secureLinkToken.setStoredInformation(storedInformation);
    secureLinkToken.setExpiryDate(expiryDate);
    secureLinkToken.setLink(link);
    secureLinkToken.setRound(constant.SECOND_ROUND);
    secureLinkToken.setEmailAddress(storeCandidateInterviewScoreRepository.fetchEmailAddress(userId));

    // Only mail the link once the score + link rows are committed, so a rolled-back
    // transaction never sends a link that doesn't exist in user_link
    Runnable sendLinkToMailService = () -> sendInterviewLinkToMailService(secureLinkToken);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          sendLinkToMailService.run();
        }
      });
    } else {
      sendLinkToMailService.run();
    }
  }

  private void sendInterviewLinkToMailService(SecureLinkToken secureLinkToken) {
    try {
      ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
          .setData(ByteString.copyFrom(objectMapper.writeValueAsBytes(secureLinkToken)))
          .build();

      pubSubTemplate.publish("user-interview-activation", pubsubMessage)
          .whenComplete((messageId, throwable) -> {
            if (throwable != null) {
              log.error("Failed to publish round-2 link to topic 'user-interview-activation'", throwable);
            } else {
              log.info("Published round-2 link {} to topic 'user-interview-activation'", messageId);
              // Same convention as round 1: status 2 = link sent
              storeCandidateInterviewScoreRepository.updateUserLinkStatus(constant.SUCCEEDD_STATUS, secureLinkToken.getToken());
            }
          });
    } catch (Exception e) {
      log.error("Could not publish round-2 link for token {}", secureLinkToken.getToken(), e);
    }
  }


  @Override
  public List<UserInterviewReport> sendInterviewScoreToUser() {
    // Iterate on the list and send call the mail service to send the mail to the user
    List<UserInterviewReport> userInterviewReportList = storeCandidateInterviewScoreRepository.fetchUserInterviewScore();
    // Send the userInterviewReportList data in the mail service
    pubSubTemplate.publish("user-interview-evaluation", userInterviewReportList)
    .whenComplete((messageId, throwable) -> {
          if (throwable!=null){
                log.error("Failed to publish message to topic 'user-detail-email'", throwable);
            }
            else{
                  log.info("Published message {} to topic 'user-detail-email'", messageId);
            }
    });
    
    return userInterviewReportList;
  }


  @Override
  public void sendEmailToInterviewEvaluationTopic(UserInterviewReport userInterviewReport) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'sendEmailToInterviewEvaluationTopi'");
  }
}