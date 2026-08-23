package com.example.extractionservice.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.extractionservice.sqlquery.SQLQuery;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaveJobEmbedding {
  private final JdbcTemplate jdbcTemplate;

  public void saveJobEmbedding (String jobDescription,float [] embeeding){
    KeyHolder keyHolder = new GeneratedKeyHolder();
    Double[] boxedEmbedding = new Double[embeeding.length];
    for (int i = 0; i < embeeding.length; i++) {
      boxedEmbedding[i] = (double) embeeding[i];
    }

    jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQLQuery.SAVE_JOB_EMBEDDING, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,jobDescription);
            ps.setArray(2, connection.createArrayOf("float8", boxedEmbedding));
            return ps;
        }, keyHolder);
  }
}
