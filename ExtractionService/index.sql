CREATE TABLE recruitment_workflow.user_data (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    resume_path VARCHAR(255) NOT NULL
);

CREATE TABLE recruitment_workflow.jobs (
    id BIGSERIAL PRIMARY KEY,
    job_description TEXT NOT NULL,
    job_description_embedding FLOAT8[] NOT NULL
);

CREATE TABLE recruitment_workflow.embedding (
    id BIGSERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES recruitment_workflow.user_data(id),
    embedding FLOAT8[] NOT NULL,
    status INT NOT NULL DEFAULT 11,
    applied_jobs INT
);
