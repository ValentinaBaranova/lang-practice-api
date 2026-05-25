ALTER TABLE teacher ADD COLUMN email VARCHAR(255);
CREATE UNIQUE INDEX idx_teacher_email ON teacher(email);
