CREATE TABLE new_roster (
    id INTEGER PRIMARY KEY NOT NULL,
    email VARCHAR(127)NOT NULL,
    role VARCHAR(31)
);

INSERT INTO new_roster (email, role) SELECT email, role FROM roster;
DROP TABLE roster;
ALTER TABLE new_roster RENAME TO roster;
