-- Migration to update fields in exercise_set.questions JSONB
-- 1. Add 'id' if missing
-- 2. Ensure 'options' exists as an array, preserving it if it already exists
-- 3. Populate 'correctAnswer' and 'gaps' from existing data, preserving 'gaps' if they exist

UPDATE exercise_set
SET questions = (
    SELECT jsonb_agg(
        (
            q 
            -- Add id if missing
            || (CASE WHEN NOT jsonb_exists(q, 'id') THEN jsonb_build_object('id', gen_random_uuid()) ELSE '{}'::jsonb END)
            -- Preserve options if it's an array, otherwise default to []
            || jsonb_build_object('options', CASE 
                WHEN jsonb_typeof(q -> 'options') = 'array' THEN q -> 'options'
                ELSE '[]'::jsonb
               END)
            -- Populate correctAnswer (deprecated field) from 'answer' if missing
            || jsonb_build_object('correctAnswer', COALESCE(q -> 'correctAnswer', q -> 'answer'))
            -- Update gaps: preserve existing ones (ensuring 'correctAnswer' field) or create from correctAnswer
            || jsonb_build_object('gaps', CASE
                WHEN jsonb_typeof(q -> 'gaps') = 'array' AND jsonb_array_length(q -> 'gaps') > 0 THEN
                    (SELECT jsonb_agg(
                        (g || (CASE WHEN jsonb_exists(g, 'answer') THEN jsonb_build_object('correctAnswer', g -> 'answer') ELSE '{}'::jsonb END)) - 'answer'
                    ) FROM jsonb_array_elements(q -> 'gaps') g)
                ELSE
                    jsonb_build_array(jsonb_build_object('index', 0, 'correctAnswer', COALESCE(q -> 'correctAnswer', q -> 'answer')))
               END)
        ) - 'answer'
    )
    FROM jsonb_array_elements(questions) q
)
WHERE questions IS NOT NULL AND jsonb_typeof(questions) = 'array' AND jsonb_array_length(questions) > 0;
