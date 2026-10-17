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

  public static String UPDATE_USER_STATUS_IN_USER_LINK = "UPDATE recruitment_workflow.user_link SET status = ? WHERE token = ?";

  public static String FETCH_USER_ID_APPLIED_JOB_ID = "SELECT user_id,applied_job_id FROM recruitment_workflow.user_link WHERE token = ?";

  public static String FETCH_USER_INTERVIEW_QUESTIONS = "SELECT questions FROM recruitment_workflow.user_question WHERE user_id = ? AND job_id = ?";

  public static String INSERT_USER_RESPONSE = "INSERT INTO recruitment_workflow.user_question_response (user_id, applied_job_id, question, response, status) VALUES (?, ?, ?, ?, ?)";

  public static String UPDATE_USER_QUESTION_RESPONSE_STATUS = "UPDATE recruitment_workflow.user_question_response SET status = ? WHERE user_id = ? AND applied_job_id = ?";

  public static String FETCH_JOB_DESCRIPTION = "SELECT job_description FROM recruitment_workflow.jobs WHERE id = ?";

  // Candidates matched to a job (embedding.status is a varchar: '2' = matched) that are still missing
  // their interview questions or their round-1 link (store_data = "userId/appliedJobId")
  public static String FIND_MATCHES_MISSING_INTERVIEW =
      "SELECT e.user_id, e.applied_jobs FROM recruitment_workflow.embedding e " +
      "WHERE e.status = '2' AND e.applied_jobs IS NOT NULL " +
      "AND (NOT EXISTS (SELECT 1 FROM recruitment_workflow.user_question q WHERE q.user_id = e.user_id AND q.job_id = e.applied_jobs) " +
      "OR NOT EXISTS (SELECT 1 FROM recruitment_workflow.user_link l WHERE l.store_data = e.user_id::text || '/' || e.applied_jobs::text))";

  public static String FETCH_RESUME_AND_JOB_DETAIL =
      "SELECT u.skills, u.experience, u.project_components, j.job_skills, j.job_experience, j.job_components " +
      "FROM recruitment_workflow.user_data u, recruitment_workflow.jobs j WHERE u.id = ? AND j.id = ?";
}
