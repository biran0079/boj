package com.boj.problem;

import com.google.common.collect.ImmutableSet;

/**
 * Created by biran on 10/31/16.
 */
public class ProblemStats {

  private final int problemId;
  private final ImmutableSet<String> solvedBy;

  ProblemStats(int problemId, ImmutableSet<String> solvedBy) {
    this.problemId = problemId;
    this.solvedBy = solvedBy;
  }

  public int getProblemId() {
    return problemId;
  }

  public boolean isSolvedBy(String userId) {
    return solvedBy.contains(userId);
  }

  public int getSolvedUserCount() {
    return solvedBy.size();
  }
}
