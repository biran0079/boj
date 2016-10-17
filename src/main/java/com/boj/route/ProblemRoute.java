package com.boj.route;

import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.problem.ProblemManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import javax.inject.Singleton;

/**
 * Created by biran on 10/17/16.
 */
@Singleton
public class ProblemRoute implements TemplateViewRoute {

  private final ProblemManager problemManager;

  @Inject
  public ProblemRoute(ProblemManager problemManager) {
    this.problemManager = problemManager;
  }

  @Override
  public ModelAndView handle(Request req, Response resp) throws Exception {
    int id = Integer.parseInt(req.params(":id"));
    ProblemRecord problemRecord = problemManager.getProblemById(id);
    if (problemRecord == null) {
      return new ModelAndView(
          Maps.newHashMap(ImmutableMap.of("message", "problem does not exist")),
          "error.html");
    }
    return new ModelAndView(
        Maps.newHashMap(ImmutableMap.of("problem", problemRecord)),
        "problem.html");
  }
}
