/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.route;

import com.boj.base.BojErrorType;
import com.boj.base.ModelAndViewFactory;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.submission.SubmissionManager;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class GetSubmitRoute implements TemplateViewRoute {

  private final SubmissionManager submissionManager;
  private final Provider<UserRecord> userProvider;
  private final ModelAndViewFactory modelAndViewFactory;

  @Inject
  public GetSubmitRoute(SubmissionManager submissionManager,
                        Provider<UserRecord> userProvider,
                        ModelAndViewFactory modelAndViewFactory) {
    this.submissionManager = submissionManager;
    this.userProvider = userProvider;
    this.modelAndViewFactory = modelAndViewFactory;
  }

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    int submissionId = Integer.valueOf(request.params(":id"));
    SubmissionRecord submission = submissionManager.getSubmissionById(submissionId);
    Map<String, Object> model = new HashMap<>();
    if (submission == null) {
      throw BojErrorType.SUBMISSION_NOT_EXIST.exception();
    } else {
      model.put("submission", submission);
      model.put("message", Strings.nullToEmpty(submission.getMessage()));
    }
    UserRecord viewer = userProvider.get();
    if (!viewer.getId().equals(submission.getUserId())) {
      submissionManager.tractSubmissionView(viewer.getId(), submissionId);
    }
    return modelAndViewFactory.create(model, "submission.html");
  }
}
