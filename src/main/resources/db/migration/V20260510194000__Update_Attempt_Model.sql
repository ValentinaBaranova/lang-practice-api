ALTER TABLE attempt RENAME COLUMN total_count TO total_questions;
ALTER TABLE attempt RENAME COLUMN correct_count TO correct_answers;
ALTER TABLE attempt ADD COLUMN answered_questions INTEGER;

CREATE TABLE attempt_question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES attempt(id),
    question_id VARCHAR(255) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
