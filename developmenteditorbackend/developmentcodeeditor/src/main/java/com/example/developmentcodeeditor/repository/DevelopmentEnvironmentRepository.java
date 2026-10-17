package com.example.developmentcodeeditor.repository;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.developmentcodeeditor.SQL.SQL;
import com.example.developmentcodeeditor.requestDTO.DevelopmentEnvironmentReponseDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DevelopmentEnvironmentRepository {

  private final JdbcTemplate jdbcTemplate;

  public List<DevelopmentEnvironmentReponseDTO> fetchFileContentTypeContent(Integer id) {
    return jdbcTemplate.query(SQL.FETCH_DEVELOPMENT_SETUP, (rs, rowNum) -> {
      byte[] content = rs.getBytes("content");
      String fileContent = content != null ? new String(content, StandardCharsets.UTF_8) : null;

      return new DevelopmentEnvironmentReponseDTO(
          rs.getString("name"),
          rs.getString("type"),
          fileContent,
          rs.getString("path_directory"));
    }, id);
  }
}
