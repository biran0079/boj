ALTER TABLE user ADD COLUMN score INTEGER DEFAULT 0;
UPDATE user SET score = 0;