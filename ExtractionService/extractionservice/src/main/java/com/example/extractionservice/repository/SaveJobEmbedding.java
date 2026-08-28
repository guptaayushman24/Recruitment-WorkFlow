package com.example.extractionservice.repository;

import java.io.UncheckedIOException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.extractionservice.constant.CONSTANT;
import com.example.extractionservice.dto.ApplyJobDTO;
import com.example.extractionservice.dto.ExtractionResumeJobDescriptionDTO;
import com.example.extractionservice.dto.ExtractionResumeJobDescriptionDTO.JobDescription;
import com.example.extractionservice.dto.ExtractionResumeJobDescriptionDTO.Resume;
import com.example.extractionservice.service.PublisherService;
import com.example.extractionservice.dto.ResumeJobMatchDTO;
import com.example.extractionservice.sqlquery.SQLQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.stemmer.snowball.porterStemmer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.CosineSimilarity;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SaveJobEmbedding {
  private final JdbcTemplate jdbcTemplate;
  private final CONSTANT constant;

  public void saveJobEmbedding (String jobDescription,float [] embeeding,List<String> jobSkills,List<String> jobExperience,List<String> jobProjectComponent){
    KeyHolder keyHolder = new GeneratedKeyHolder();
    Double[] boxedEmbedding = new Double[embeeding.length];
    for (int i = 0; i < embeeding.length; i++) {
      boxedEmbedding[i] = (double) embeeding[i];
    }

    jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQLQuery.SAVE_JOB_EMBEDDING, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,jobDescription);
            ps.setArray(2, connection.createArrayOf("float8", boxedEmbedding));
            ps.setArray(3,connection.createArrayOf("text", jobSkills.toArray(new String[0])));
            ps.setArray(4,connection.createArrayOf("text", jobExperience.toArray(new String[0])));
            ps.setArray(5, connection.createArrayOf("text", jobProjectComponent.toArray(new String[0])));
            return ps;
        }, keyHolder);
  }

  public void applyJob (ApplyJobDTO applyJobDTO){
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(SQLQuery.APPLY_JOB);
      ps.setInt(1, applyJobDTO.getJobId());
      ps.setInt(2, applyJobDTO.getUserId());
      return ps;
    });
  }

  public List<ResumeJobMatchDTO> findMatchUserResumeJobDescription() throws JsonProcessingException{
    return jdbcTemplate.query(SQLQuery.FIND_MATCH_USER_JOB_DESCRIPTION, (rs, rowNum) -> {
      ResumeJobMatchDTO dto = new ResumeJobMatchDTO();
      dto.setEmbeddingId(rs.getLong("embedding_id"));
      dto.setUserId(rs.getInt("user_id"));
      dto.setAppliedJobs(rs.getInt("applied_jobs"));
      dto.setEmbedding(toFloatArray(rs.getArray("embedding")));
      dto.setSkills(toStringList(rs.getArray("skills")));
      dto.setExperience(toStringList(rs.getArray("experience")));
      dto.setProjectComponents(toStringList(rs.getArray("project_components")));
      dto.setJobSkills(toStringList(rs.getArray("job_skills")));
      dto.setJobExperience(toStringList(rs.getArray("job_experience")));
      dto.setJobComponents(toStringList(rs.getArray("job_components")));
      dto.setJobDescriptionEmbedding(toFloatArray(rs.getArray("job_description_embedding")));
      return dto;
    });
  }

  private static float[] toFloatArray(Array sqlArray) throws SQLException {
    if (sqlArray == null) {
      return new float[0];
    }
    Double[] boxed = (Double[]) sqlArray.getArray();
    float[] result = new float[boxed.length];
    for (int i = 0; i < boxed.length; i++) {
      result[i] = boxed[i].floatValue();
    }
    return result;
  }

  private static List<String> toStringList(Array sqlArray) throws SQLException {
    if (sqlArray == null) {
      return List.of();
    }
    String[] values = (String[]) sqlArray.getArray();
    return Arrays.asList(values);
  }

  public void userJobUpdateStatus (Integer status, Integer userId, Integer appliedJobs){
    jdbcTemplate.update(SQLQuery.UPDATE_EMBEDDING_STATUS, status, userId, appliedJobs);
  }
}
