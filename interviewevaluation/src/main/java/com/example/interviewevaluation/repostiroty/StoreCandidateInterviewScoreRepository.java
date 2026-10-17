package com.example.interviewevaluation.repostiroty;

import java.time.LocalDateTime;
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
  // Exceptions are deliberately not swallowed here: the caller runs in a transaction and
  // needs them to roll back the score row together with the round-2 link.
  public int saveCandidateInterviewScore (Integer userId,Integer appliedJobId,Double candidateInterviewScore,Integer status,Integer interviewRound){
    return jdbcTemplate.update(SQL.INSERT_INTO_USER_INTERVIEW_SCORE, userId, appliedJobId, candidateInterviewScore, status,interviewRound);
  }

  public boolean userLinkExists (Integer userId,Integer appliedJobId,Integer round){
    Integer count = jdbcTemplate.queryForObject(SQL.COUNT_USER_LINK_BY_ROUND, Integer.class, userId, appliedJobId, round);
    return count != null && count > 0;
  }

  public int saveUserLink (String token,String storedInformation,Integer userId,Integer appliedJobId,LocalDateTime expiryDate,String link,Integer status,Integer round){
    return jdbcTemplate.update(SQL.INSERT_INTO_USER_LINK, token, storedInformation, userId, appliedJobId, expiryDate, link, status, round);
  }

  public String fetchEmailAddress (Integer userId){
    return jdbcTemplate.queryForObject(SQL.FETCH_EMAIL_ADDRESS, String.class, userId);
  }

  public void updateUserLinkStatus (Integer status,String token){
    jdbcTemplate.update(SQL.UPDATE_USER_LINK_STATUS, status, token);
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
