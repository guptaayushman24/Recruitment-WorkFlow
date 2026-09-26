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
}
