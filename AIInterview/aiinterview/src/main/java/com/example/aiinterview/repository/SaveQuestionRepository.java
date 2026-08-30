package com.example.aiinterview.repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.aiinterview.dto.SecureLinkToken;
import com.example.aiinterview.sql.SQL;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaveQuestionRepository {
  private final JdbcTemplate jdbcTemplate;

  public Integer generateQuestions (Integer userId, Integer appliedJobId, List<String> questionGeneratedByAI){
    Integer existingQuestionCount = jdbcTemplate.queryForObject(
      SQL.COUNT_USER_INTERVIEW_QUESTIONS, Integer.class, userId, appliedJobId);

    if (existingQuestionCount != null && existingQuestionCount > 0) {
      return 1;
    }

    List<String> questions = questionGeneratedByAI.stream()
    .flatMap(s -> Arrays.stream(s.split(",")))
    .map(String::trim)
    .collect(Collectors.toList());

    if (questions.isEmpty()) {
      return 0;
    }

    for (int i = 0; i < questions.size(); i++) {
      int rowsAffected = jdbcTemplate.update(SQL.INSERT_USER_INTERVIEW_QUESTIONS, userId, appliedJobId, i + 1, questions.get(i));
      if (rowsAffected != 1) {
        return 0;
      }
    }
    return 1;
  }

  public boolean userInterViewLinkExists (Integer userId, String storedInformation){
    Integer existingLinkCount = jdbcTemplate.queryForObject(
      SQL.COUNT_USER_LINK, Integer.class, userId, storedInformation);
    return existingLinkCount != null && existingLinkCount > 0;
  }

  public int generateUserInterViewLink (SecureLinkToken secureLinkToken,Integer userId,Integer appliedJobId,Integer status){
    int rowsAffected = jdbcTemplate.update(
      SQL.INSERT_INTO_USER_LINK,secureLinkToken.getToken(),secureLinkToken.getStoredInformation(),userId,appliedJobId,secureLinkToken.getExpiryDate(),secureLinkToken.getLink(),status
    );

    if (rowsAffected!=1){
      return 0;
    }

    return 1;
  }
  
  public String userEmailAddress (Integer userId){
    return jdbcTemplate.queryForObject(SQL.FETCH_EMAIL_ADDRESS, String.class, userId);
  }

  public Integer checkIntivitationAlreadySendToUser (Integer userId,Integer appliedJobId){
    return jdbcTemplate.queryForObject(SQL.IS_ACTIVATION_LINK_SEND_TO_USER, Integer.class,userId,appliedJobId);
  }

  public void updateUserActivationLink (Integer successConstant,Integer userId,Integer appliedJobId){
    jdbcTemplate.update(SQL.UPDATE_ACTIVATION_LINK_OF_USER,successConstant,userId,appliedJobId);
  }


}
