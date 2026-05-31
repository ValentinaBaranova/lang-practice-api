-- Migration to update share_slug by prepending the slugified title
-- This ensures that exercise sets have their title in the URL for better SEO and readability.

UPDATE exercise_set
SET share_slug = substring(
    trim(trailing '-' from substring(trim(both '-' from regexp_replace(regexp_replace(lower(title), '[^a-z0-9]+', '-', 'g'), '-+', '-', 'g')), 1, 50))
    || '-' || share_slug
, 1, 255)
WHERE title IS NOT NULL AND title <> ''
  AND share_slug IS NOT NULL
  AND share_slug <> trim(trailing '-' from substring(trim(both '-' from regexp_replace(regexp_replace(lower(title), '[^a-z0-9]+', '-', 'g'), '-+', '-', 'g')), 1, 50))
  AND NOT (share_slug LIKE trim(trailing '-' from substring(trim(both '-' from regexp_replace(regexp_replace(lower(title), '[^a-z0-9]+', '-', 'g'), '-+', '-', 'g')), 1, 50)) || '-%');
