CREATE TABLE submission_view (
    id INTEGER PRIMARY KEY NOT NULL,
    submission_id INTEGER NOT NULL,
    viewer_user_id VARCHAR(63) NOT NULL,
    datetime INTEGER
);
