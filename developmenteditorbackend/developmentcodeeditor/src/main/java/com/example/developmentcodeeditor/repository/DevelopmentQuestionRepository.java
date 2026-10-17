package com.example.developmentcodeeditor.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.developmentcodeeditor.SQL.SQL;
import com.example.developmentcodeeditor.requestDTO.DevelopmentQuestionResponseDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DevelopmentQuestionRepository {

  private final JdbcTemplate jdbcTemplate;

  public List<DevelopmentQuestionResponseDTO> fetchDevelopmentQuestions() {
    return jdbcTemplate.query(SQL.FETCH_DEVELOPMENT_QUESTIONS, (rs, rowNum) -> new DevelopmentQuestionResponseDTO(
        rs.getInt("id"),
        rs.getString("question_name"),
        rs.getInt("expected_status"),
        rs.getString("expected_endpoint"),
        rs.getString("expected_response")));
  }

  /** Endpoint the user has to implement for the question, or null if the question does not exist. */
  public String fetchExpectedEndpoint(Integer questionId) {
    List<String> endpoints = jdbcTemplate.queryForList(SQL.FETCH_EXPECTED_ENDPOINT, String.class, questionId);
    return endpoints.isEmpty() ? null : endpoints.get(0);
  }

  /** Stores what the user's API returned for the question; updates the existing row if the question already has one. */
  public void saveUserResponse(Integer questionId, String userResponse, Integer userStatus, String userEndpoint) {
    int updated = jdbcTemplate.update(SQL.UPDATE_DEVELOPMENT_USER_RESPONSE, userResponse, userStatus, userEndpoint, questionId);
    if (updated == 0) {
      jdbcTemplate.update(SQL.INSERT_DEVELOPMENT_USER_RESPONSE, questionId, userResponse, userStatus, userEndpoint);
    }
  }
}
