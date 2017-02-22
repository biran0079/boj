DELETE FROM submission WHERE user_id IS NULL;
DELETE FROM submission_view WHERE id NOT IN (SELECT DISTINCT id FROM submission);