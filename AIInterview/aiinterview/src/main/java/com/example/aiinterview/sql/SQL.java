package com.example.aiinterview.sql;

public class SQL {
  public static String INSERT_USER_INTERVIEW_QUESTIONS = "INSERT INTO recruitment_workflow.user_question (user_id, job_id, question_order, questions) VALUES (?, ?, ?, ?)";

  public static String COUNT_USER_INTERVIEW_QUESTIONS = "SELECT COUNT(*) FROM recruitment_workflow.user_question WHERE user_id = ? AND job_id = ?";

  public static String COUNT_USER_LINK = "SELECT COUNT(*) FROM recruitment_workflow.user_link WHERE user_id = ? AND store_data = ?";

  public static String INSERT_INTO_USER_LINK = "INSERT INTO recruitment_workflow.user_link (token,store_data,user_id,applied_job_id,expiry_date,link,status) VALUES (?,?,?,?,?,?,?)";

  public static String FETCH_EMAIL_ADDRESS = "SELECT email FROM recruitment_workflow.user_data WHERE id = ?";

  public static String IS_ACTIVATION_LINK_SEND_TO_USER = "SELECT status FROM recruitment_workflow.user_link WHERE user_id = ? and applied_job_id = ?";

  public static String UPDATE_ACTIVATION_LINK_OF_USER = "UPDATE recruitment_workflow.user_link SET status = ? WHERE user_id = ? AND applied_job_id = ?"; 

  public static String FETCH_STATUS_EXPIRY_DATE = "SELECT expiry_date,status FROM recruitment_workflow.user_link where token = ?";
}