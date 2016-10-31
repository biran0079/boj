package com.boj.problem;

import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.Field;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.boj.jooq.tables.Problem.PROBLEM;

/**
 * Created by biran on 10/17/16.
 */
@Singleton
public class ProblemManager {

  private final DSLContext db;
  private final Provider<UserRecord> currentUser;
  private final ProblemStatsManager problemStatsManager;

  @Inject
  public ProblemManager(DSLContext db,
                        Provider<UserRecord> currentUser,
                        ProblemStatsManager problemStatsManager) {
    this.db = db;
    this.currentUser = currentUser;
    this.problemStatsManager = problemStatsManager;
  }

  private List<ProblemRecord> getProblems() {
    return ImmutableList.copyOf(db.selectFrom(PROBLEM).fetch());
  }

  public List<ProblemData> getProblemsData() {
    return getProblems().stream()
        .map(record -> new ProblemData(record,
            problemStatsManager.getProblemStat(record.getId()),
            currentUser.get()))
        .collect(Collectors.toList());
  }

  public ProblemRecord getProblemById(int id) {
    return db.selectFrom(PROBLEM)
        .where(PROBLEM.ID.eq(id))
        .fetchOne();
  }

  public ProblemRecord createProblem(String title, String description, String template, String test) {
    ProblemRecord problem = db.newRecord(PROBLEM);
    problem.setTitle(title);
    problem.setDescription(description);
    problem.setTemplateSrc(template);
    problem.setJunitTestSrc(test);
    problem.store();
    return problem;
  }

  public ProblemRecord updateProblem(int problemId,
                                     String title,
                                     String description,
                                     String template,
                                     String test) {
    ProblemRecord problem = getProblemById(problemId);
    problem.setTitle(title);
    problem.setDescription(description);
    problem.setTemplateSrc(template);
    problem.setJunitTestSrc(test);
    problem.store();
    return problem;
  }

  public void deleteProblem(int problemId) {
    db.deleteFrom(PROBLEM).where(PROBLEM.ID.eq(problemId)).execute();
  }

  public Map<Integer, ProblemRecord> getProblemsByIds(Set<Integer> problemids) {
    Map<Integer, ProblemRecord> problems = new HashMap<>();
    for (ProblemRecord record : db.selectFrom(PROBLEM)
        .where(PROBLEM.ID.in(problemids))
        .fetch()) {
      problems.put(record.getId(), record);
    }
    return problems;
  }

  private static final List<Field<?>> IMPORT_EXPORT_FIELDS = ImmutableList.of(
      PROBLEM.TITLE,
      PROBLEM.DESCRIPTION,
      PROBLEM.TEMPLATE_SRC,
      PROBLEM.JUNIT_TEST_SRC);

  public void importProblemFromjson(String json) throws IOException {
    db.loadInto(PROBLEM)
        .loadJSON(json)
        .fields(IMPORT_EXPORT_FIELDS)
        .execute();
  }

  public String exportProblemByIdAsJson(int id) {
    return db.select(IMPORT_EXPORT_FIELDS)
        .from(PROBLEM)
        .where(PROBLEM.ID.eq(id))
        .fetch()
        .formatJSON();
  }
}
