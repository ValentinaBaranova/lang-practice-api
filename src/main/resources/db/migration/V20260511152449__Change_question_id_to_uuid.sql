-- Change question_id from VARCHAR to UUID in attempt_question table
-- First, ensure all existing data can be converted (they should be UUID strings or empty)
-- Since it's a new project, we can just cast it.

ALTER TABLE attempt_question 
ALTER COLUMN question_id TYPE UUID USING question_id::UUID;
