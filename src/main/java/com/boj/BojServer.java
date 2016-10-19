package com.boj;

import com.boj.base.DateUtil;
import com.boj.base.MapBuilder;
import com.boj.base.ModelAndViewFactory;
import com.boj.filter.AuthenticationFilter;
import com.boj.guice.RequestScope;
import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.jooq.tables.records.TestCaseRecord;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.problem.ProblemManager;
import com.boj.route.CreateOrUpdateProblemRoute;
import com.boj.route.GetSubmitRoute;
import com.boj.route.SubmitRoute;
import com.boj.route.UserRoute;
import com.boj.submission.SubmissionManager;
import com.boj.user.UserManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.template.pebble.PebbleTemplateEngine;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * Created by biran on 10/12/16.
 */
@Singleton
public class BojServer {

  private static final Logger logger = LoggerFactory.getLogger(BojServer.class);

  private final Flyway flyway;
  private final CreateOrUpdateProblemRoute createOrUpdateProblemRoute;
  private final SubmitRoute submitRoute;
  private final UserRoute userRoute;
  private final AuthenticationFilter authFilter;
  private final GetSubmitRoute getSubmitRoute;
  private final RequestScope requestScope;
  private final ProblemManager problemManager;
  private final UserManager userManager;
  private final SubmissionManager submissionManager;
  private final ModelAndViewFactory modelAndViewFactory;

  @Inject
  public BojServer(Flyway flyway,
                   CreateOrUpdateProblemRoute createOrUpdateProblemRoute,
                   SubmitRoute submitRoute,
                   GetSubmitRoute getSubmitRoute,
                   AuthenticationFilter authFilter,
                   UserRoute userRoute,
                   RequestScope requestScope,
                   ProblemManager problemManager,
                   UserManager userManager,
                   SubmissionManager submissionManager,
                   ModelAndViewFactory modelAndViewFactory) {
    this.flyway = flyway;
    this.createOrUpdateProblemRoute = createOrUpdateProblemRoute;
    this.submitRoute = submitRoute;
    this.getSubmitRoute = getSubmitRoute;
    this.authFilter = authFilter;
    this.userRoute = userRoute;
    this.requestScope = requestScope;
    this.problemManager = problemManager;
    this.userManager = userManager;
    this.submissionManager = submissionManager;
    this.modelAndViewFactory = modelAndViewFactory;
  }

  void start() {
    flyway.migrate();
    staticFiles.location("/static");
    exception(Exception.class, (exception, request, response) -> {
      logger.error("Error when handling {}", request.pathInfo(), exception);
      response.redirect("/error");
    });

    port(8080);
    threadPool(Runtime.getRuntime().availableProcessors());

    ClasspathLoader loader = new ClasspathLoader();
    loader.setPrefix("templates/");

    before("/*", (request, response) -> requestScope.enter());
    before("/*", authFilter);

    after("/*", (request, response) -> requestScope.exit());

    PebbleTemplateEngine engine = new PebbleTemplateEngine(loader);

    get("/", (req, resp) -> modelAndViewFactory.create(new HashMap<>(), "index.html"), engine);

    get("/error", (req, resp) -> modelAndViewFactory.create(new HashMap<>(), "error.html"), engine);

    get("/problems", (request, response) -> {
      return modelAndViewFactory.create(
          MapBuilder.create().put("problems", problemManager.getProblems()).build(),
          "problems.html");
    }, engine);

    get("/problem/create", (req, resp) -> modelAndViewFactory.create(
        Maps.newHashMap(ImmutableMap.of(
            "button", "create",
            "problem", new ProblemRecord(),
            "test", new TestCaseRecord()
        )), "problem_edit.html"), engine);
    get("/problem/edit/:id", (req, resp) -> {
      int problemId = Integer.valueOf(req.params(":id"));
      ProblemRecord problem = problemManager.getProblemById(problemId);
      TestCaseRecord testCase = problemManager.getTestCaseForProblem(problemId);
      return modelAndViewFactory.create(
          Maps.newHashMap(ImmutableMap.of(
              "button", "update",
              "problem", problem,
              "test", testCase)),
          "problem_edit.html");
    }, engine);

    post("/problem", createOrUpdateProblemRoute);
    get("/problem/:id", (request, response) -> {
      int id = Integer.parseInt(request.params(":id"));
      ProblemRecord problemRecord = problemManager.getProblemById(id);
      if (problemRecord == null) {
        return modelAndViewFactory.create(
            Maps.newHashMap(ImmutableMap.of("message", "problem does not exist")),
            "error.html");
      }
      return modelAndViewFactory.create(
          Maps.newHashMap(ImmutableMap.of("problem", problemRecord)),
          "problem.html");
    }, engine);
    // TODO: require admin
    delete("/problem/:id", (request, response) -> {
      int problemId = Integer.valueOf(request.params(":id"));
      problemManager.deleteProblemAndTestCase(problemId);
      return "ok";
    });

    get("/submits", (request, response) -> {
      List<SubmissionRecord> submissionRecordList = submissionManager.getSubmissions();
      Map<Integer, ProblemRecord> problemRecordMap = problemManager.getProblemsByIds(
          submissionRecordList.stream()
              .map(SubmissionRecord::getProblemId)
              .collect(Collectors.toSet()));
      Map<String, UserRecord> userRecordMap = userManager.getUsersByIds(
          submissionRecordList.stream()
              .map(SubmissionRecord::getUserId)
              .collect(Collectors.toSet()));
      return modelAndViewFactory.create(
          MapBuilder.create()
              .put("submissions", submissionRecordList.stream()
                  .map(record -> MapBuilder.create()
                      .put("record", record)
                      .put("problem", problemRecordMap.get(record.getProblemId()))
                      .put("user", userRecordMap.get(record.getUserId()))
                      .put("datetime", DateUtil.format(record.getDatetime()))
                      .build())
                  .collect(Collectors.toList()))
              .build(), "submissions.html");
    }, engine);


    get("/user/:id", userRoute, engine);
    post("/submit/:id", submitRoute);
    get("/submit/:id", getSubmitRoute, engine);
  }
}
