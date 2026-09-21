package com.example.codeeditor.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.codeeditor.responsedto.CodingQuestion;
import com.example.codeeditor.sql.SQL;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StartCodingInterviewRepository {
  private final JdbcTemplate jdbcTemplate;

  public List<CodingQuestion> fetchCodingQuestions() {
    return jdbcTemplate.query(SQL.FETCH_CODING_QUESTIONS, (rs, rowNum) -> {
      CodingQuestion codingQuestion = new CodingQuestion();
      codingQuestion.setId(rs.getLong("id"));
      codingQuestion.setTitle(rs.getString("question_title"));
      codingQuestion.setCodingQuestion(rs.getString("question_description"));
      return codingQuestion;
    });
  }
}
