package com.boj;

import com.boj.problems.ProblemSet;
import com.boj.problems.ProblemSets;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

/**
 * Created by biran on 10/12/16.
 */
public class BojServer {

  private final TestRunner testRunner = new TestRunner();
  private final ProblemSet problemSet = ProblemSets.SAMPLE;

  void start() {
    port(8080);
    threadPool(Runtime.getRuntime().availableProcessors());
    get("/", new IndexRoute(), new VelocityTemplateEngine());
    before("/*", new AuthenticationFilter());
    get("/problems", new ProblemsRoute(problemSet), new VelocityTemplateEngine());
    post("/submit/:id", new SubmitRoute(testRunner, problemSet),  new VelocityTemplateEngine());
  }
}
