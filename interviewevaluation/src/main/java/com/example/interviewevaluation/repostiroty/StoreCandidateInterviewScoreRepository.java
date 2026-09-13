package com.example.interviewevaluation.repostiroty;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.interviewevaluation.dto.UserInterviewReport;
import com.example.interviewevaluation.sql.SQL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j 
public class StoreCandidateInterviewScoreRepository {
  private final JdbcTemplate jdbcTemplate;
  public int saveCandidateInterviewScore (Integer userId,Integer appliedJobId,Double candidateInterviewScore,Integer status){
    try{
      return jdbcTemplate.update(SQL.INSERT_INTO_USER_INTERVIEW_SCORE, userId, appliedJobId, candidateInterviewScore, status);
    }
    catch (Exception e){
      log.info("Some error occured in saving the data in the database ::::::: {}",e);
      return -1;
    }

  }

  public List<UserInterviewReport> fetchUserInterviewScore(){
    try{
      return jdbcTemplate.query(SQL.FETCH_USER_INTERVIEW_SCORE, (rs, rowNum) -> {
        UserInterviewReport userInterviewReport = new UserInterviewReport();
        userInterviewReport.setEmail(rs.getString("email"));
        userInterviewReport.setJobTitle(rs.getString("job_title"));
        userInterviewReport.setUserScore(rs.getDouble("score"));
        return userInterviewReport;
      });
    }
    catch (Exception e){
      log.error("Some exception is occured in fetching the candidate score ::::: {}",e);
      return List.of();
    }
  }
  
}
