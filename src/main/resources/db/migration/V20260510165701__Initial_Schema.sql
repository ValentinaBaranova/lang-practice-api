CREATE TABLE teacher (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exercise_set (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id UUID NOT NULL REFERENCES teacher(id),
    title VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    questions JSONB NOT NULL,
    share_slug VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attempt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exercise_set_id UUID NOT NULL REFERENCES exercise_set(id),
    student_name VARCHAR(255),
    correct_count INTEGER,
    total_count INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
