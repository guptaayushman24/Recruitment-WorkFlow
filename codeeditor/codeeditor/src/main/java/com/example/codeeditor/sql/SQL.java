package com.example.codeeditor.sql;


public class SQL {
  public static final String FETCH_CODING_QUESTIONS = "select rwcrq.id, rwcrq.question_title, rwcrq.question_description, "
      + "rwcqt.driver_code, rwcqt.user_code_template, "
      + "jsonb_agg(jsonb_build_object('input', rwcrtq.input, 'output', rwcrtq.output)) as test_cases "
      + "from recruitment_workflow.coding_round_questions as rwcrq "
      + "join recruitment_workflow.coding_round_questions_test_case as rwcrtq on rwcrq.id = rwcrtq.crq_id "
      + "join recruitment_workflow.coding_question_tempate as rwcqt on rwcrq.id = rwcqt.crq_id "
      + "where rwcrq.id = ? "
      + "group by rwcrq.id, rwcrq.question_title, rwcrq.question_description, rwcqt.driver_code, rwcqt.user_code_template";

  public static final String FETCH_MAXIMUM_NUMBER_OF_QUESTIONS = "select COUNT (*) from recruitment_workflow.coding_round_questions";

  public static final String FETCH_USER_LINK_BY_TOKEN = "select status, expiry_date, round from recruitment_workflow.user_link where token = ?";

  public static final String INSERT_INTO_USER_INTERVIEW_SCORE_SECOND_ROUND = "INSERT INTO recruitment_workflow.user_interview_score (user_id, job_id, score, status,round) VALUES (?,?,?,?,?)";

  public static final String UPDATE_USER_INTERVIEW_SCORE_SECOND_ROUND = "UPDATE recruitment_workflow.user_interview_score SET score = ? WHERE user_id = ? AND job_id = ? AND round = ?";

  public static final String FETCH_USER_AND_JOB_BY_TOKEN = "SELECT user_id, applied_job_id, round FROM recruitment_workflow.user_link WHERE token = ?";
}
