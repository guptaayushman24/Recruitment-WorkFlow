package com.example.interviewevaluation.serviceimpl;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.interviewevaluation.ai.AnswerEvaluation;
import com.example.interviewevaluation.ai.InterviewEvaluationAssistant;
import com.example.interviewevaluation.constant.Constant;
import com.example.interviewevaluation.dto.UserInterviewReport;
import com.example.interviewevaluation.repostiroty.StoreCandidateInterviewScoreRepository;
import com.example.interviewevaluation.service.InterviewEvaluationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewEvaluationServiceImpl implements InterviewEvaluationService {
  private final InterviewEvaluationAssistant interviewEvaluationAssistant;
  private final RedisTemplate<String, Double> redisTemplate;
  private final StoreCandidateInterviewScoreRepository storeCandidateInterviewScoreRepository;
  private final Constant constant;

  @Override
  public Integer getInterviewScore(String jobDescription, String question, String answer, Integer userId,
      Integer appliedJobId) {
    try {
      AnswerEvaluation candidateAnswer = interviewEvaluationAssistant.evaluateAnswer(jobDescription, question, answer);
      String compositeKey = String.format("userId:%s:appliedJobId:%s", userId, appliedJobId);
      redisTemplate.opsForValue().set(compositeKey, candidateAnswer.getScore());
      return 1;
    } catch (Exception e) {
      log.error("Some error is occured in storing the final score in the reddis");
      return 0;
    }
  }


  @Override
  public void saveCandidateInterviewScore(Integer userId, Integer appliedJobId) {
    String compositeKey = String.format("userId:%s:appliedJobId:%s", userId, appliedJobId);
    Double candidateInterviewScore = redisTemplate.opsForValue().get(compositeKey);
    storeCandidateInterviewScoreRepository.saveCandidateInterviewScore(userId,appliedJobId,candidateInterviewScore,constant.PENDING_STATUS);
    
  }


  @Override
  public List<UserInterviewReport> sendInterviewScoreToUser() {
    // Iterate on the list and send call the mail service to send the mail to the user
    List<UserInterviewReport> userInterviewReport = storeCandidateInterviewScoreRepository.fetchUserInterviewScore();
    // Iterate here and call the mail service to send the mail to the user
    return userInterviewReport;
  }
}