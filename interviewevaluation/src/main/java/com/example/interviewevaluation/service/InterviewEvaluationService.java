package com.example.interviewevaluation.service;

import java.util.List;

import com.example.interviewevaluation.dto.UserInterviewReport;

public interface InterviewEvaluationService {
  public Integer getInterviewScore(String jobDescription,String question,String answer,Integer userId,Integer appliedJobId);
  public void saveCandidateInterviewScore (Integer userId,Integer appliedJobId);
  public List<UserInterviewReport> sendInterviewScoreToUser ();
}
