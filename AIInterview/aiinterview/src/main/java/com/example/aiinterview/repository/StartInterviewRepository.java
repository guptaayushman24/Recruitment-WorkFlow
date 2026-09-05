package com.example.aiinterview.repository;

import com.example.aiinterview.constant.CONSTANT;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.aiinterview.dto.UserIdAppliedJobId;
import com.example.aiinterview.sql.SQL;

import lombok.RequiredArgsConstructor;

@Repository 
@RequiredArgsConstructor 
public class StartInterviewRepository {
  private final CONSTANT CONSTANT;
  private final JdbcTemplate jdbcTemplate;

  public int startInterview (String token){
    return jdbcTemplate.update(SQL.UPDATE_USER_STATUS_IN_USER_LINK, CONSTANT.USED, token);
  }

  public UserIdAppliedJobId fetchUserIdAndAppliedJobId (String token){
    return jdbcTemplate.queryForObject(
      SQL.FETCH_USER_ID_APPLIED_JOB_ID,
      (rs, rowNum) -> {
        UserIdAppliedJobId userIdAppliedJobId = new UserIdAppliedJobId();
        userIdAppliedJobId.setUserId(rs.getInt("user_id"));
        userIdAppliedJobId.setAppliedJobId(rs.getInt("applied_job_id"));
        return userIdAppliedJobId;
      },
      token);
  }

  public List<String> fetchUserInterviewQuestions (Integer userId,Integer appliedJobId){
    List<String> userInterviewQuestions = jdbcTemplate.query(
      SQL.FETCH_USER_INTERVIEW_QUESTIONS,
      (rs, rowNum) -> rs.getString("questions"),
      userId,appliedJobId);

    return userInterviewQuestions;
  }

}
