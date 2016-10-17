/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.submission;

import com.boj.jooq.tables.Submission;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.judge.Verdict;
import com.google.inject.Singleton;
import org.jooq.DSLContext;

import javax.inject.Inject;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class SubmissionManager {

  private final DSLContext db;

  @Inject
  public SubmissionManager(DSLContext db) {
    this.db = db;
  }

  public SubmissionRecord createSubmission(int problemId, String userId, String solution, Verdict verdict) {
    SubmissionRecord submissionRecord = db.newRecord(Submission.SUBMISSION);
    submissionRecord.setProblemId(problemId);
    submissionRecord.setUserId(userId);
    submissionRecord.setSubmittedSrc(solution);
    submissionRecord.setVerdict(verdict.toString());
    submissionRecord.store();
    return submissionRecord;
  }

  public void updateVerdictAndMessage(int submissionId, Verdict verdict, String message) {
    db.update(Submission.SUBMISSION)
        .set(Submission.SUBMISSION.VERDICT, verdict.toString())
        .set(Submission.SUBMISSION.MESSAGE, message)
        .where(Submission.SUBMISSION.ID.eq(submissionId))
        .execute();
  }

  public SubmissionRecord getSubmissionById(int submissionId) {
    return db.selectFrom(Submission.SUBMISSION)
        .where(Submission.SUBMISSION.ID.eq(submissionId))
        .fetchOne();
  }
}
