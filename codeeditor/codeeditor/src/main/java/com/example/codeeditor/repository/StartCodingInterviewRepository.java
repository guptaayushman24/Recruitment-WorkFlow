package com.example.codeeditor.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.codeeditor.requestdto.SampleCodingTestCaseDTO;
import com.example.codeeditor.responsedto.CodingQuestion;
import com.example.codeeditor.sql.SQL;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class StartCodingInterviewRepository {
  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;

  public List<CodingQuestion> fetchCodingQuestions(Integer customRange) {
    return jdbcTemplate.query(SQL.FETCH_CODING_QUESTIONS, (rs, rowNum) -> {
      CodingQuestion codingQuestion = new CodingQuestion();
      codingQuestion.setId(rs.getLong("id"));
      codingQuestion.setTitle(rs.getString("question_title"));
      codingQuestion.setCodingQuestion(rs.getString("question_description"));
      codingQuestion.setTestCases(parseTestCases(rs.getString("test_cases")));
      codingQuestion.setQuestionCodingTemplate(rs.getString("driver_code"));
      codingQuestion.setUserCodingTemplate(rs.getString("user_code_template"));
      return codingQuestion;
    },customRange);
  }

  private List<SampleCodingTestCaseDTO> parseTestCases(String testCasesJson) {
    return objectMapper.readValue(testCasesJson, new TypeReference<List<SampleCodingTestCaseDTO>>() {});
  }

  public Integer maxiMumQuestions (){
    return jdbcTemplate.queryForObject(SQL.FETCH_MAXIMUM_NUMBER_OF_QUESTIONS, (rs,rowNum)-> rs.getInt(1));
  }
}
