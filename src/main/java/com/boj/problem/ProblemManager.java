package com.boj.problem;

import com.boj.jooq.tables.Problem;
import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.TestCaseRecord;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.boj.jooq.tables.TestCase.TEST_CASE;

/**
 * Created by biran on 10/17/16.
 */
@Singleton
public class ProblemManager {

  private final DSLContext db;

  @Inject
  public ProblemManager(DSLContext db) {
    this.db = db;
  }

  public List<ProblemRecord> getProblems() {
    return ImmutableList.copyOf(db.selectFrom(Problem.PROBLEM).fetch());
  }

  public ProblemRecord getProblemById(int id) {
    return db.selectFrom(Problem.PROBLEM)
        .where(Problem.PROBLEM.ID.eq(id))
        .fetchOne();
  }

  public TestCaseRecord getTestCaseForProblem(int problemId) {
    return db.selectFrom(TEST_CASE)
        .where(TEST_CASE.PROBLEM_ID.eq(problemId))
        .fetchOne();
  }

  public ProblemRecord createProblem(String title, String description, String template) {
    ProblemRecord problem = db.newRecord(Problem.PROBLEM);
    problem.setTitle(title);
    problem.setDescription(description);
    problem.setTemplateSrc(template);
    problem.store();
    return problem;
  }

  public ProblemRecord updateProblem(int problemId, String title, String description, String template) {
    ProblemRecord problem = getProblemById(problemId);
    problem.setTitle(title);
    problem.setDescription(description);
    problem.setTemplateSrc(template);
    problem.store();
    return problem;
  }

  public TestCaseRecord createTestCase(int problemId, String testSrc) {
    TestCaseRecord testCaseRecord = db.newRecord(TEST_CASE);
    testCaseRecord.setProblemId(problemId);
    testCaseRecord.setJunitTestSrc(testSrc);
    testCaseRecord.store();
    return testCaseRecord;
  }

  public void deleteProblemAndTestCase(int problemId) {
    db.deleteFrom(Problem.PROBLEM).where(Problem.PROBLEM.ID.eq(problemId)).execute();
    db.deleteFrom(TEST_CASE).where(TEST_CASE.PROBLEM_ID.eq(problemId)).execute();
  }

  public TestCaseRecord updateTestCase(int problemId, String testSrc) {
    TestCaseRecord testCase = getTestCaseForProblem(problemId);
    testCase.setJunitTestSrc(testSrc);
    testCase.store();
    return testCase;
  }

  public Map<Integer, ProblemRecord> getProblemsByIds(Set<Integer> problemids) {
    Map<Integer, ProblemRecord> problems = new HashMap<>();
    for (ProblemRecord record : db.selectFrom(Problem.PROBLEM)
        .where(Problem.PROBLEM.ID.in(problemids))
        .fetch()) {
      problems.put(record.getId(), record);
    }
    return problems;
  }
}
