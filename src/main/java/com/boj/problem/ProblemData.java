package com.boj.problem;

import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.UserRecord;

/**
 * Created by biran on 10/30/16.
 */
public class ProblemData {

  private final ProblemRecord record;
  private final ProblemStats problemStats;
  private final UserRecord me;

  ProblemData(ProblemRecord record, ProblemStats problemStats, UserRecord me) {
    this.record = record;
    this.problemStats = problemStats;
    this.me = me;
  }

  public int getId() {
    return record.getId();
  }

  public String getTitle() {
    return record.getTitle();
  }

  public String getDescription() {
    return record.getDescription();
  }

  public String getJunitTestSrc() {
    return record.getJunitTestSrc();
  }

  public String getTemplateSrc() {
    return record.getTemplateSrc();
  }

  public boolean getSolvedByMe() {
    return problemStats.isSolvedBy(me.getId());
  }

  public int getSolvedUserCount() {
    return problemStats.getSolvedUserCount();
  }
}
