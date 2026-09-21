CREATE TABLE coding_round_questions (
    id SERIAL PRIMARY KEY,
    question_title VARCHAR(255) NOT NULL,
    question_description TEXT NOT NULL
);

INSERT INTO coding_round_questions (question_title, question_description)
VALUES ('Reverse a String', 'Write a function that takes a string as input and returns the string reversed.');
