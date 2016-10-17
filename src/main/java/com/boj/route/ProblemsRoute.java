package com.boj.route;

import com.boj.annotation.IsAdmin;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.problem.ProblemManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Created by biran on 10/14/16.
 */
@Singleton
public class ProblemsRoute implements TemplateViewRoute {

  private final ProblemManager problemManager;
  private final Provider<UserRecord> userRecordProvider;
  private final Provider<Boolean> isAdmin;

  @Inject
  public ProblemsRoute(ProblemManager problemManager,
                       Provider<UserRecord> userRecordProvider,
                       @IsAdmin
                       Provider<Boolean> isAdmin) {
    this.problemManager = problemManager;
    this.userRecordProvider = userRecordProvider;
    this.isAdmin = isAdmin;
  }

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    return new ModelAndView(
        Maps.newHashMap(ImmutableMap.of(
            "problems", problemManager.getProblems(),
            "is_admin", isAdmin.get())),
        "problems.html");
  }
}
