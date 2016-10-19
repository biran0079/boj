ALTER TABLE submission ADD COLUMN datetime INTEGER;
UPDATE submission SET datetime = 0;
