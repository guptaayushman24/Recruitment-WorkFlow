package com.example.aiinterview.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.aiinterview.dto.TokenStatusExpiryDTO;
import com.example.aiinterview.sql.SQL;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ValidateLinkRepository {
  private final JdbcTemplate jdbcTemplate;

  public TokenStatusExpiryDTO fetchStatusAndExpiryDate(String token) {
    return jdbcTemplate.queryForObject(
      SQL.FETCH_STATUS_EXPIRY_DATE,
      (rs, rowNum) -> {
        TokenStatusExpiryDTO dto = new TokenStatusExpiryDTO();
        dto.setStatus(rs.getInt("status"));
        dto.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
        return dto;
      },
      token);
  }
}
