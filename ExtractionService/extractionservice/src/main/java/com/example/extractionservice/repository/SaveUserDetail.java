package com.example.extractionservice.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.extractionservice.model.UserData;
import com.example.extractionservice.sqlquery.SQLQuery;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SaveUserDetail {

    private final JdbcTemplate jdbcTemplate;

    public void saveUserDetail(UserData userData) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQLQuery.SAVE_USER_DETAIL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, userData.getFirstName());
            ps.setString(2, userData.getLastName());
            ps.setString(3, userData.getEmail());
            ps.setString(4, userData.getResumePath());
            return ps;
        }, keyHolder);
    }

    public UserData fetchUserById(Long id) {
        return jdbcTemplate.queryForObject(SQLQuery.FETCH_USER_BY_ID, (rs, rowNum) ->
                UserData.builder()
                        .id(rs.getLong("id"))
                        .firstName(rs.getString("first_name"))
                        .lastName(rs.getString("last_name"))
                        .email(rs.getString("email"))
                        .resumePath(rs.getString("resume_path"))
                        .build(),
                id);
    }
}
