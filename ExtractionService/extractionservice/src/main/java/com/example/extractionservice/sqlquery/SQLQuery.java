package com.example.extractionservice.sqlquery;

public class SQLQuery {

    public static final String SAVE_USER_DETAIL =
            "INSERT INTO recruitment_workflow.user_data (first_name, last_name, email, resume_path) VALUES (?, ?, ?, ?)";

    public static final String FETCH_USER_BY_ID =
            "SELECT id, first_name, last_name, email, resume_path FROM recruitment_workflow.user_data WHERE id = ?";

    public static final String FETCH_USER_ID_FROM_EMAIL = "SELECT id FROM recruitment_workflow.user_data WHERE email = ?";

    public static final String SAVE_USER_EMBEDDING = "INSERT INTO recruitment_workflow.embedding (user_id,embedding,status,applied_jobs) VALUES (?,?,?,?)";
}
