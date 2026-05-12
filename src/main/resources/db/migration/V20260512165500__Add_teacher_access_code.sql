-- Add access_code column as nullable first
ALTER TABLE teacher ADD COLUMN access_code VARCHAR(10);

-- Update existing teachers with random-looking access codes
UPDATE teacher SET access_code = SUBSTR(MD5(RANDOM()::TEXT), 1, 10) WHERE access_code IS NULL;

-- Update the default teacher with a specific access code for tests
UPDATE teacher SET access_code = 'DEFAULT001' WHERE id = '00000000-0000-0000-0000-000000000000';

-- Make access_code NOT NULL
ALTER TABLE teacher ALTER COLUMN access_code SET NOT NULL;

-- Add UNIQUE constraint
ALTER TABLE teacher ADD CONSTRAINT teacher_access_code_key UNIQUE (access_code);
