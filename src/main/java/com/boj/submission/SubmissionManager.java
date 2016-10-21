/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.submission;

import com.boj.base.DateUtil;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.jooq.tables.records.SubmissionViewRecord;
import com.boj.judge.Verdict;
import com.google.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record1;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.boj.jooq.Tables.SUBMISSION;
import static com.boj.jooq.Tables.SUBMISSION_VIEW;

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
    SubmissionRecord submissionRecord = db.newRecord(SUBMISSION);
    submissionRecord.setProblemId(problemId);
    submissionRecord.setUserId(userId);
    submissionRecord.setSubmittedSrc(solution);
    submissionRecord.setVerdict(verdict.toString());
    submissionRecord.setDatetime(DateUtil.now());
    submissionRecord.store();
    return submissionRecord;
  }

  public void updateVerdictAndMessage(int submissionId, Verdict verdict, String message) {
    db.update(SUBMISSION)
        .set(SUBMISSION.VERDICT, verdict.toString())
        .set(SUBMISSION.MESSAGE, message)
        .where(SUBMISSION.ID.eq(submissionId))
        .execute();
  }

  public SubmissionRecord getSubmissionById(int submissionId) {
    return db.selectFrom(SUBMISSION)
        .where(SUBMISSION.ID.eq(submissionId))
        .fetchOne();
  }

  public List<SubmissionRecord> getSubmissionsForUser(String userId) {
    return getSubmissionsForUser(userId, Integer.MAX_VALUE);
  }

  public List<SubmissionRecord> getSubmissionsForUser(String userId, int limit) {
    return db.selectFrom(SUBMISSION)
        .where(SUBMISSION.USER_ID.eq(userId))
        .orderBy(SUBMISSION.DATETIME.desc())
        .limit(limit)
        .fetch();
  }

  public List<SubmissionRecord> getSubmissions() {
    return db.selectFrom(SUBMISSION)
        .orderBy(SUBMISSION.DATETIME.desc())
        .fetch();
  }

  public SubmissionViewRecord tractSubmissionView(String userId, int submissionId) {
    SubmissionViewRecord record = db.newRecord(SUBMISSION_VIEW);
    record.setDatetime(DateUtil.now());
    record.setSubmissionId(submissionId);
    record.setViewerUserId(userId);
    record.store();
    return record;
  }

  public List<SubmissionViewRecord> getSubmissionViewRecords(String userId, int limit) {
    List<Integer> submissionViewIds = new ArrayList<>();
    for (Record1<Integer> record : db.select(SUBMISSION_VIEW.ID)
        .from(SUBMISSION_VIEW
            .join(SUBMISSION)
            .on(SUBMISSION_VIEW.SUBMISSION_ID.eq(SUBMISSION.ID)))
        .where(SUBMISSION.USER_ID.eq(userId))
        .limit(limit)
        .fetch()) {
      submissionViewIds.add(record.value1());
    }
    return db.selectFrom(SUBMISSION_VIEW)
        .where(SUBMISSION_VIEW.ID.in(submissionViewIds))
        .fetch();
  }
}
