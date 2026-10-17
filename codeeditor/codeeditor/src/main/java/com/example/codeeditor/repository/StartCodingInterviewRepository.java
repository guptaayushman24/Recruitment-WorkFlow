package com.example.codeeditor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

  // Empty when the token doesn't exist
  public Optional<UserLink> fetchUserLinkByToken(String token) {
    return jdbcTemplate.query(SQL.FETCH_USER_LINK_BY_TOKEN, (rs, rowNum) -> new UserLink(
        rs.getInt("status"),
        rs.getTimestamp("expiry_date").toLocalDateTime(),
        rs.getInt("round")), token).stream().findFirst();
  }

  // Empty when the token doesn't exist
  public Optional<UserJob> fetchUserAndJobByToken(String token) {
    return jdbcTemplate.query(SQL.FETCH_USER_AND_JOB_BY_TOKEN, (rs, rowNum) -> new UserJob(
        rs.getInt("user_id"),
        rs.getInt("applied_job_id"),
        rs.getInt("round")), token).stream().findFirst();
  }

  // One round-2 row per candidate and job: the score is updated as more test cases are run
  public void insertIntoUserInterviewScore (Integer userId,Integer jobId,Integer score,Integer status,Integer round){
    int updated = jdbcTemplate.update(SQL.UPDATE_USER_INTERVIEW_SCORE_SECOND_ROUND, score, userId, jobId, round);
    if (updated == 0) {
      jdbcTemplate.update(SQL.INSERT_INTO_USER_INTERVIEW_SCORE_SECOND_ROUND, userId, jobId, score, status, round);
    }
  }

  public record UserLink(Integer status, LocalDateTime expiryDate, Integer round) {}

  public record UserJob(Integer userId, Integer appliedJobId, Integer round) {}
}
