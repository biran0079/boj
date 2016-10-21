package com.boj;

import com.boj.annotation.IsAdmin;
import com.boj.base.*;
import com.boj.filter.AuthenticationFilter;
import com.boj.guice.RequestScope;
import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.SubmissionRecord;
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
import org.apache.commons.io.IOUtils;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.template.pebble.PebbleTemplateEngine;

import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.Charset;
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
  private final PermissionDeniedExceptionHandler permissionDeniedExceptionHandler;
  private final Provider<Boolean> isAdmin;

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
                   ModelAndViewFactory modelAndViewFactory,
                   PermissionDeniedExceptionHandler permissionDeniedExceptionHandler,
                   @IsAdmin Provider<Boolean> isAdmin) {
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
    this.permissionDeniedExceptionHandler = permissionDeniedExceptionHandler;
    this.isAdmin = isAdmin;
  }

  void start() {
    flyway.migrate();
    staticFiles.location("/static");
    exception(PermissionDeniedException.class, permissionDeniedExceptionHandler);
    exception(Exception.class, (exception, request, response) -> {
      logger.error("Error when handling {}", request.pathInfo(), exception);
    });

    port(8080);
    threadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

    ClasspathLoader loader = new ClasspathLoader();
    loader.setPrefix("templates/");

    before("/*", (request, response) -> requestScope.enter());
    before("/*", authFilter);

    after("/*", (request, response) -> requestScope.exit());

    PebbleTemplateEngine engine = new PebbleTemplateEngine(loader);

    get("/", (req, resp) -> modelAndViewFactory.create(new HashMap<>(), "index.html"), engine);

    get("/error", (req, resp) -> modelAndViewFactory.create(new HashMap<>(), "error.html"), engine);

    get("/problems", (request, response) -> modelAndViewFactory.create(
        MapBuilder.create().put("problems", problemManager.getProblems()).build(),
        "problems.html"), engine);

    get("/problem/create", (req, resp) -> modelAndViewFactory.create(
        Maps.newHashMap(ImmutableMap.of(
            "button", "create",
            "problem", new ProblemRecord()
        )), "problem_edit.html"), engine);
    get("/problem/edit/:id", (req, resp) -> {
      int problemId = Integer.valueOf(req.params(":id"));
      ProblemRecord problem = problemManager.getProblemById(problemId);
      return modelAndViewFactory.create(
          Maps.newHashMap(ImmutableMap.of(
              "button", "update",
              "problem", problem)),
          "problem_edit.html");
    }, engine);

    post("/problem", createOrUpdateProblemRoute);
    get("/problem/:id", (request, response) -> {
      int id = Integer.parseInt(request.params(":id"));
      ProblemRecord problemRecord = problemManager.getProblemById(id);
      if (problemRecord == null) {
        return modelAndViewFactory.create(
            MapBuilder.create()
                .put("message", "problem does not exist")
                .build(),
            "error.html");
      }
      return modelAndViewFactory.create(
          MapBuilder.create()
              .put("problem", problemRecord)
              .put("isAdmin", isAdmin.get())
              .build(),
          "problem.html");
    }, engine);
    delete("/problem/:id", (request, response) -> {
      if (!isAdmin.get()) {
        throw new PermissionDeniedException();
      }
      int problemId = Integer.valueOf(request.params(":id"));
      problemManager.deleteProblem(problemId);
      return "ok";
    });

    get("/problem/export/:id", ((req, res) -> {
      int id = Integer.valueOf(req.params(":id"));
      res.type("Application/octetstream");
      res.header("Content-Disposition", String.format("attachment; filename=\"%s\"", "problem_" + id + ".json"));
      HttpServletResponse raw = res.raw();
      raw.getOutputStream().write(problemManager.exportProblemByIdAsJson(id).getBytes());
      raw.getOutputStream().flush();
      raw.getOutputStream().close();
      return raw;
    }));

    post("/problem/import", ((req, res) -> {
      req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
      try (InputStream is = req.raw().getPart("problem_file").getInputStream()) {
        String json = IOUtils.toString(is, Charset.defaultCharset());
        problemManager.importProblemFromjson(json);
      }
      res.redirect("/problems");
      return null;
    }));

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
