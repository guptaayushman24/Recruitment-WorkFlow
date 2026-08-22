package com.example.extractionservice.sqlquery;

public class SQLQuery {

    public static final String SAVE_USER_DETAIL =
            "INSERT INTO recruitment_workflow.user_data (first_name, last_name, email, resume_path) VALUES (?, ?, ?, ?)";

    public static final String FETCH_USER_BY_ID =
            "SELECT id, first_name, last_name, email, resume_path FROM recruitment_workflow.user_data WHERE id = ?";
}
