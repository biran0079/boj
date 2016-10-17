package com.boj;

import com.boj.filter.AuthenticationFilter;
import com.boj.guice.RequestScope;
import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.jooq.tables.records.TestCaseRecord;
import com.boj.problem.ProblemManager;
import com.boj.route.*;
import com.boj.submission.SubmissionManager;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by biran on 10/12/16.
 */
@Singleton
public class BojServer {

  private static final Logger logger = LoggerFactory.getLogger(BojServer.class);;

  private final Flyway flyway;
  private final ProblemsRoute problemsRoute;
  private final CreateOrUpdateProblemRoute createOrUpdateProblemRoute;
  private final SubmitRoute submitRoute;
  private final AuthenticationFilter authFilter;
  private final ProblemRoute problemRoute;
  private final RequestScope requestScope;
  private final ProblemManager problemManager;
  private final SubmissionManager submissionManager;

  @Inject
  public BojServer(Flyway flyway,
                   ProblemsRoute problemsRoute,
                   CreateOrUpdateProblemRoute createOrUpdateProblemRoute,
                   SubmitRoute submitRoute,
                   AuthenticationFilter authFilter,
                   ProblemRoute problemRoute,
                   RequestScope requestScope,
                   ProblemManager problemManager,
                   SubmissionManager submissionManager) {
    this.flyway = flyway;
    this.problemsRoute = problemsRoute;
    this.createOrUpdateProblemRoute = createOrUpdateProblemRoute;
    this.submitRoute = submitRoute;
    this.authFilter = authFilter;
    this.problemRoute = problemRoute;
    this.requestScope = requestScope;
    this.problemManager = problemManager;
    this.submissionManager = submissionManager;
  }

  void start() {
    flyway.migrate();
    exception(Exception.class, (exception, request, response) -> {
      logger.error("Error when handling {}", request.pathInfo(), exception);
    });

    port(8080);
    threadPool(Runtime.getRuntime().availableProcessors());

    VelocityTemplateEngine engine = new VelocityTemplateEngine();

    before("/*", (request, response) -> requestScope.enter());
    before("/*", authFilter);

    after("/*", (request, response) -> requestScope.exit());

    get("/", (req, resp) -> new ModelAndView(new HashMap<>(), "index.html"), engine);

    get("/problems", problemsRoute, engine);

    get("/problem/create", (req, resp) -> new ModelAndView(
        Maps.newHashMap(ImmutableMap.of(
            "button", "create",
            "problem", new ProblemRecord(),
            "test", new TestCaseRecord()
        )), "problem_edit.html"), engine);
    get("/problem/edit/:id", (req, resp) -> {
      int problemId = Integer.valueOf(req.params(":id"));
      ProblemRecord problem = problemManager.getProblemById(problemId);
      TestCaseRecord testCase = problemManager.getTestCaseForProblem(problemId);
      return new ModelAndView(
          Maps.newHashMap(ImmutableMap.of(
              "button", "update",
              "problem", problem,
              "test", testCase
          )), "problem_edit.html");
      }, engine);

    post("/problem", createOrUpdateProblemRoute);
    get("/problem/:id", problemRoute, engine);
    delete("/problem/:id", (request, response) -> {
      int problemId = Integer.valueOf(request.params(":id"));
      problemManager.deleteProblemAndTestCase(problemId);
      return "ok";
    });

    post("/submit/:id", submitRoute);
    get("/submit/:id", (request, response) -> {
      int submissionId = Integer.valueOf(request.params(":id"));
      SubmissionRecord submission = submissionManager.getSubmissionById(submissionId);
      Map<String, Object> model = new HashMap<>();
      if (submission == null) {
        model.put("verdict", "submission not found");
        model.put("message", "");
      } else {
        model.put("verdict", submission.getVerdict().toString());
        model.put("message", Strings.nullToEmpty(submission.getMessage()));
      }
      return new ModelAndView(model, "submission.html");
    }, engine);
  }
}
