
CREATE TABLE coding_round_questions_test_case (
    id SERIAL PRIMARY KEY,
    crq_id INTEGER NOT NULL REFERENCES recruitment_workflow.coding_round_questions(id),
    input JSONB NOT NULL,
    output JSONB NOT NULL
);
