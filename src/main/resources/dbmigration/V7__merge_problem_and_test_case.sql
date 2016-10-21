ALTER TABLE problem ADD COLUMN junit_test_src TEXT;
UPDATE problem SET junit_test_src = (SELECT junit_test_src FROM test_case where test_case.problem_id = problem.id);
DROP TABLE test_case;
