package com.boj.prefupgrade;

import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.judge.Verdict;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.boj.jooq.Tables.PREF_UPGRADE;
import static com.boj.jooq.Tables.SUBMISSION;
import static com.boj.jooq.Tables.USER;

/**
 * Created by biran on 1/9/17.
 */
public class PrefUpgrader {

  private static final int MAX_VERSION = 1;
  private final DSLContext db;

  @Inject
  public PrefUpgrader(DSLContext db) {
    this.db = db;
  }

  public void upgrade() {
    int version = db.select(PREF_UPGRADE.VERSION).from(PREF_UPGRADE).fetchOne().value1();
    if (version == MAX_VERSION) {
      return;
    }
    while (version < MAX_VERSION) {
      version++;
      switch (version) {
        case 1:
          recomputeScore();
          break;
      }
    }
    db.update(PREF_UPGRADE).set(PREF_UPGRADE.VERSION, version).execute();
  }

  private void recomputeScore() {
    Map<String, Integer> scores = new HashMap<>();
    Map<String, Set<Integer>> solved = new HashMap<>();
    for (SubmissionRecord record : db.selectFrom(SUBMISSION).fetch()) {
      String userId = record.getUserId();
      if (!solved.containsKey(userId)) {
        solved.put(userId, new HashSet<>());
      }
      Verdict verdict = Verdict.valueOf(record.getVerdict());
      switch (verdict) {
        case ACCEPTED:
          if (solved.get(userId).add(record.getProblemId())) {
            scores.put(userId, scores.getOrDefault(userId, 0) + 10);
          }
          break;
        case WRONG_ANSWER:
        case TIME_LIMIT_EXCEEDED:
        case MEMORY_LIMIT_EXCEEDED:
        case RUNTIME_ERROR:
          scores.put(userId, scores.getOrDefault(userId, 0) - 2);
          break;
      }
    }
    scores.forEach((userId, score) -> {
      db.update(USER)
          .set(USER.SCORE, Math.max(0, score))
          .where(USER.ID.eq(userId))
          .execute();
    });
  }
}
