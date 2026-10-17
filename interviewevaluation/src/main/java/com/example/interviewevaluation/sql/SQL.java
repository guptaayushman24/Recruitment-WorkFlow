package com.example.interviewevaluation.sql;

public class SQL {
  public static String INSERT_INTO_USER_INTERVIEW_SCORE = "INSERT INTO recruitment_workflow.user_interview_score (user_id, job_id, score, status,round) VALUES (?,?,?,?,?)";

  public static String FETCH_USER_INTERVIEW_SCORE =
      "UPDATE recruitment_workflow.user_interview_score rwusc " +
      "SET status = 2 " +
      "FROM recruitment_workflow.jobs rwj, recruitment_workflow.user_data rwusd " +
      "WHERE rwusc.job_id = rwj.id " +
      "AND rwusc.user_id = rwusd.id " +
      "AND rwusc.status = 1 " +
      "RETURNING rwusd.email, rwj.job_title, rwusc.score";

  public static String COUNT_USER_LINK_BY_ROUND = "SELECT COUNT(*) FROM recruitment_workflow.user_link WHERE user_id = ? AND applied_job_id = ? AND round = ?";

  public static String INSERT_INTO_USER_LINK = "INSERT INTO recruitment_workflow.user_link (token,store_data,user_id,applied_job_id,expiry_date,link,status,round) VALUES (?,?,?,?,?,?,?,?)";

  public static String FETCH_EMAIL_ADDRESS = "SELECT email FROM recruitment_workflow.user_data WHERE id = ?";

  public static String UPDATE_USER_LINK_STATUS = "UPDATE recruitment_workflow.user_link SET status = ? WHERE token = ?";
}
