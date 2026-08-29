package com.example.aiinterview.repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.aiinterview.sql.SQL;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaveQuestionRepository {
  private final JdbcTemplate jdbcTemplate;

  public void generateQuestions (Integer userId, Integer appliedJobId, List<String> questionGeneratedByAI){
    List<String> questions = questionGeneratedByAI.stream()
    .flatMap(s -> Arrays.stream(s.split(",")))
    .map(String::trim)
    .collect(Collectors.toList());

    for (int i = 0; i < questions.size(); i++) {
      jdbcTemplate.update(SQL.INSERT_USER_INTERVIEW_QUESTIONS, userId, appliedJobId, i + 1, questions.get(i));
    }
  }
}
