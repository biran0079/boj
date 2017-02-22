CREATE TABLE user (
    id VARCHAR(63) PRIMARY KEY NOT NULL,
    email VARCHAR(127)
);

CREATE TABLE problem (
    id INTEGER PRIMARY KEY NOT NULL,
    title VARCHAR(63),
    description TEXT,
    template_src TEXT
);

CREATE TABLE test_case (
    problem_id INTEGER PRIMARY KEY NOT NULL,
    junit_test_src TEXT
);

CREATE TABLE submission (
    id INTEGER PRIMARY KEY NOT NULL,
    problem_id INTEGER,
    user_id VARCHAR(63),
    submitted_src TEXT,
    verdict VARCHAR(31)
);
