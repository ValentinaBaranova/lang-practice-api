CREATE TABLE telegram_user (
    id UUID PRIMARY KEY,
    chat_id BIGINT NOT NULL UNIQUE,
    topic VARCHAR(255),
    is_subscribed BOOLEAN NOT NULL DEFAULT FALSE,
    last_exercise_sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
