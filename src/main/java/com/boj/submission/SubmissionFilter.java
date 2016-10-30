package com.boj.submission;

import com.boj.jooq.tables.Submission;
import com.boj.judge.Verdict;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import static com.boj.jooq.tables.Submission.SUBMISSION;

/**
 * Created by biran on 10/30/16.
 */
public class SubmissionFilter {

  private final String userId;
  private final Integer problemId;
  private final String verdict;

  private SubmissionFilter(Builder builder) {
    this.userId = builder.userId;
    this.problemId = builder.problemId;
    this.verdict = builder.verdict;
  }

  public Condition toJooqCondition() {
    Condition result = DSL.trueCondition();
    if (userId != null) {
      result = result.and(SUBMISSION.USER_ID.eq(userId));
    }
    if (problemId != null) {
      result = result.and(SUBMISSION.PROBLEM_ID.eq(problemId));
    }
    if (verdict != null) {
      result = result.and(SUBMISSION.VERDICT.eq(verdict));
    }
    return result;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String userId;
    private Integer problemId;
    private String verdict;

    private Builder() {

    }

    public Builder setuserId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder setProblemId(int problemId) {
      this.problemId = problemId;
      return this;
    }

    public Builder setVerdict(String verdict) {
      this.verdict = verdict;
      return this;
    }

    public SubmissionFilter build() {
      return new SubmissionFilter(this);
    }
  }
}
