package com.example.codeeditor.sql;


public class SQL {
  public static final String FETCH_CODING_QUESTIONS = "select id,question_title,question_description from recruitment_workflow.coding_round_questions where id = ?";

  public static final String FETCH_MAXIMUM_NUMBER_OF_QUESTIONS = "select COUNT (*) from recruitment_workflow.coding_round_questions";

  public static final String FETCH_CODING_TEST_CASE = "select input,output from recruitment_workflow.coding_round_questions_test_case";
}
