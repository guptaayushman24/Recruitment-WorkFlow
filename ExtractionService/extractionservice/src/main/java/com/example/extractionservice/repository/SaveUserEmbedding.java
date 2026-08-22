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
public class SaveUserEmbedding {
  private final JdbcTemplate jdbcTemplate;

  public void saveUserEmbedding (Integer userId,float [] embeeding,Integer status,Integer appliedJobs){
    KeyHolder keyHolder = new GeneratedKeyHolder();

    Double[] boxedEmbedding = new Double[embeeding.length];
    for (int i = 0; i < embeeding.length; i++) {
      boxedEmbedding[i] = (double) embeeding[i];
    }

     jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQLQuery.SAVE_USER_EMBEDDING, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setArray(2, connection.createArrayOf("float8", boxedEmbedding));
            ps.setInt(3, status);
            ps.setInt(4, appliedJobs);
            return ps;
        }, keyHolder);
  }
}
