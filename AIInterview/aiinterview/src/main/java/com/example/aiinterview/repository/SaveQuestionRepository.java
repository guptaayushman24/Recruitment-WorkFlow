package com.example.aiinterview.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.aiinterview.constant.CONSTANT;
import com.example.aiinterview.dto.ExtractionResumeJobDescriptionDTO;
import com.example.aiinterview.dto.SecureLinkToken;
import com.example.aiinterview.dto.UserIdAppliedJobId;
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

  public Integer saveUserResponse (Integer userId,Integer appliedJobId,String question,String response,Integer status){
    return jdbcTemplate.update(SQL.INSERT_USER_RESPONSE, ps -> {
      ps.setInt(1, userId);
      ps.setInt(2, appliedJobId);
      ps.setArray(3, ps.getConnection().createArrayOf("text", new String[]{question}));
      ps.setArray(4, ps.getConnection().createArrayOf("text", new String[]{response}));
      ps.setInt(5, status);
    });
  }

  public void updateUserQuestionResponseStatus (Integer status,Integer userId,Integer appliedJobId){
    jdbcTemplate.update(SQL.UPDATE_USER_QUESTION_RESPONSE_STATUS, status, userId, appliedJobId);
  }

  public List<UserIdAppliedJobId> findMatchesMissingInterview (){
    return jdbcTemplate.query(SQL.FIND_MATCHES_MISSING_INTERVIEW, (rs, rowNum) -> {
      UserIdAppliedJobId match = new UserIdAppliedJobId();
      match.setUserId(rs.getInt("user_id"));
      match.setAppliedJobId(rs.getInt("applied_jobs"));
      return match;
    });
  }

  // Rebuilds the payload extractionservice originally published, from what it already stored
  public ExtractionResumeJobDescriptionDTO fetchResumeAndJobDetail (Integer userId,Integer appliedJobId){
    return jdbcTemplate.queryForObject(SQL.FETCH_RESUME_AND_JOB_DETAIL, (rs, rowNum) -> {
      ExtractionResumeJobDescriptionDTO dto = new ExtractionResumeJobDescriptionDTO();

      ExtractionResumeJobDescriptionDTO.Resume resume = new ExtractionResumeJobDescriptionDTO.Resume();
      resume.setSkills(toStringList(rs.getArray("skills")));
      resume.setExperience(toStringList(rs.getArray("experience")));
      resume.setProjectComponents(toStringList(rs.getArray("project_components")));

      ExtractionResumeJobDescriptionDTO.JobDescription job = new ExtractionResumeJobDescriptionDTO.JobDescription();
      job.setJobSkills(toStringList(rs.getArray("job_skills")));
      job.setJobExperience(toStringList(rs.getArray("job_experience")));
      job.setJobComponents(toStringList(rs.getArray("job_components")));

      ExtractionResumeJobDescriptionDTO.UserDetail userDetail = new ExtractionResumeJobDescriptionDTO.UserDetail();
      userDetail.setUserId(userId);
      userDetail.setAppliedJobId(appliedJobId);

      dto.setResumeExtraction(resume);
      dto.setJobDescriptionExtraction(job);
      dto.setUserDetail(userDetail);
      return dto;
    }, userId, appliedJobId);
  }

  private static List<String> toStringList (java.sql.Array sqlArray) throws java.sql.SQLException {
    return sqlArray == null ? List.of() : java.util.Arrays.asList((String[]) sqlArray.getArray());
  }
}
