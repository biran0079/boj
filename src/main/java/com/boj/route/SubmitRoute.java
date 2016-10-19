package com.boj.route;

import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.judge.Judge;
import com.boj.judge.Verdict;
import com.boj.problem.ProblemManager;
import com.boj.submission.SubmissionManager;
import com.google.inject.Inject;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Created by biran on 10/12/16.
 */
@Singleton
public class SubmitRoute implements Route {

  private final Judge judge;
  private final SubmissionManager submissionManager;
  private final Provider<UserRecord> user;

  @Inject
  public SubmitRoute(Judge judge,
                     SubmissionManager submissionManager,
                     Provider<UserRecord> user) {
    this.judge = judge;
    this.submissionManager = submissionManager;
    this.user = user;
  }

  @Override
  public Object handle(Request req, Response resp) throws Exception {
    int problemId = Integer.parseInt(req.params(":id"));
    String solution = req.queryParams("solution");
    SubmissionRecord submission =  submissionManager
        .createSubmission(problemId, user.get().getId(), solution, Verdict.PENDING);
    judge.submit(submission);
    return submission.getId();
  }
}
