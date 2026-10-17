package com.example.developmentcodeeditor.SQL;

public class SQL {
  public static String FETCH_DEVELOPMENT_SETUP = "select name,type,content,path_directory from recruitment_workflow.development_framework_setup where project_id = ?";
  // TODO: filter by user once the user_id join is wired up
  public static String FETCH_DEVELOPMENT_QUESTIONS = "select id,question_name,expected_status,expected_endpoint,expected_response from recruitment_workflow.development_question order by id";
  public static String FETCH_EXPECTED_ENDPOINT = "select expected_endpoint from recruitment_workflow.development_question where id = ?";
  public static String INSERT_DEVELOPMENT_USER_RESPONSE = "insert into recruitment_workflow.development_user_response (question_id,user_response,user_status,user_endpoint) values (?,?,?,?)";
  public static String UPDATE_DEVELOPMENT_USER_RESPONSE = "update recruitment_workflow.development_user_response set user_response = ?, user_status = ?, user_endpoint = ? where question_id = ?";
}
