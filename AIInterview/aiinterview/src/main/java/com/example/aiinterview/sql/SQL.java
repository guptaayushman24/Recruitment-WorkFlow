package com.example.aiinterview.sql;

public class SQL {
  public static String INSERT_USER_INTERVIEW_QUESTIONS = "INSERT INTO recruitment_workflow.user_question (user_id, job_id, question_order, questions) VALUES (?, ?, ?, ?)";
}