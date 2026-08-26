package com.example.extractionservice.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.extractionservice.dto.ApplyJobDTO;
import com.example.extractionservice.sqlquery.SQLQuery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SaveJobEmbedding {
  private final JdbcTemplate jdbcTemplate;

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
}
