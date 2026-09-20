package com.example.interviewevaluation.sql;

public class SQL {
  public static String INSERT_INTO_USER_INTERVIEW_SCORE = "INSERT INTO recruitment_workflow.user_interview_score (user_id, job_id, score, status) VALUES (?,?,?,?)";

  public static String FETCH_USER_INTERVIEW_SCORE =
      "UPDATE recruitment_workflow.user_interview_score rwusc " +
      "SET status = 2 " +
      "FROM recruitment_workflow.jobs rwj, recruitment_workflow.user_data rwusd " +
      "WHERE rwusc.job_id = rwj.id " +
      "AND rwusc.user_id = rwusd.id " +
      "AND rwusc.status = 1 " +
      "RETURNING rwusd.email, rwj.job_title, rwusc.score";
}
