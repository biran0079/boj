/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.route;

import com.boj.base.ModelAndViewFactory;
import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.jooq.tables.records.SubmissionViewRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.problem.ProblemManager;
import com.boj.submission.SubmissionManager;
import com.boj.user.UserManager;
import com.boj.base.DateUtil;
import com.boj.base.MapBuilder;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class UserRoute implements TemplateViewRoute {

  private final UserManager userManager;
  private final SubmissionManager submissionManager;
  private final ProblemManager problemManager;
  private final ModelAndViewFactory modelAndViewFactory;

  @Inject
  public UserRoute(UserManager userManager,
                   SubmissionManager submissionManager,
                   ProblemManager problemManager,
                   ModelAndViewFactory modelAndViewFactory) {
    this.userManager = userManager;
    this.submissionManager = submissionManager;
    this.problemManager = problemManager;
    this.modelAndViewFactory = modelAndViewFactory;
  }

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    UserRecord user = userManager.getUser(request.params(":id"));
    List<SubmissionRecord> submissionRecordList = submissionManager.getSubmissionsForUser(user.getId(), 5);
    Map<Integer, ProblemRecord> problems = problemManager.getProblemsByIds(
        submissionRecordList.stream()
            .map(SubmissionRecord::getProblemId)
            .collect(Collectors.toSet()));
    List<SubmissionViewRecord> submissionViews = submissionManager.getSubmissionViewRecords(user.getId(), 5);
    Map<String, UserRecord> users = userManager.getUsersByIds(submissionViews.stream()
        .map(SubmissionViewRecord::getViewerUserId)
        .collect(Collectors.toSet()));

    return modelAndViewFactory.create(MapBuilder.create()
        .put("submissions", submissionRecordList.stream()
            .map(record -> MapBuilder.create()
                .put("record", record)
                .put("problem", problems.get(record.getProblemId()))
                .put("user", user)
                .put("datetime", DateUtil.format(record.getDatetime()))
                .build())
            .collect(Collectors.toList()))
        .put("submission_views", submissionViews.stream()
            .map(record -> MapBuilder.create()
                .put("record", record)
                .put("viewer", users.get(record.getViewerUserId()))
                .put("datetime", DateUtil.format(record.getDatetime()))
                .build())
            .collect(Collectors.toList()))
        .build(),
        "user.html");
  }
}
