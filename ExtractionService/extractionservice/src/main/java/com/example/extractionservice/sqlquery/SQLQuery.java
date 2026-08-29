package com.example.extractionservice.sqlquery;

public class SQLQuery {

    public static final String SAVE_USER_DETAIL =
            "INSERT INTO recruitment_workflow.user_data (first_name, last_name, email, resume_path) VALUES (?, ?, ?, ?)";

    public static final String FETCH_USER_BY_ID =
            "SELECT id, first_name, last_name, email, resume_path FROM recruitment_workflow.user_data WHERE id = ?";

    public static final String FETCH_USER_ID_FROM_EMAIL = "SELECT id FROM recruitment_workflow.user_data WHERE email = ?";

    public static final String SAVE_USER_EMBEDDING = "INSERT INTO recruitment_workflow.embedding (user_id,embedding,status,applied_jobs) VALUES (?,?,?,?)";

    public static final String SAVE_JOB_EMBEDDING =
        "INSERT INTO recruitment_workflow.jobs (job_description, job_description_embedding,job_skills,job_experience,job_components) VALUES (?, ?,?,?,?)";

    public static final String UPDATE_USER_DATA =
        "UPDATE recruitment_workflow.user_data SET skills = ?, experience = ?, project_components = ? WHERE id = ?";

    public static final String APPLY_JOB =
        "UPDATE recruitment_workflow.embedding SET applied_jobs = ? WHERE user_id = ?";

    public static final String FIND_MATCH_USER_JOB_DESCRIPTION =
        "SELECT " +
        "e.id AS embedding_id, " +
        "e.user_id, " +
        "e.applied_jobs, " +
        "e.embedding, " +
        "u.skills, " +
        "u.experience, " +
        "u.project_components, " +
        "j.id, "+
        "j.job_skills, " +
        "j.job_experience, " +
        "j.job_components, " +
        "j.job_description_embedding " +
        "FROM recruitment_workflow.embedding e " +
        "JOIN recruitment_workflow.user_data u ON e.user_id = u.id " +
        "JOIN recruitment_workflow.jobs j ON e.applied_jobs = j.id " +
        "WHERE e.status = '1'";

        public static final String UPDATE_EMBEDDING_STATUS =
            "UPDATE recruitment_workflow.embedding SET status = ? WHERE user_id = ? AND applied_jobs = ?";
}
