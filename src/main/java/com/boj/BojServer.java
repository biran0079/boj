package com.boj;

import com.boj.problems.ProblemSet;
import com.boj.problems.ProblemSets;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by biran on 10/12/16.
 */
public class BojServer {

  private final TestRunner testRunner = new TestRunner();
  private final ProblemSet problemSet = ProblemSets.SAMPLE;

  void start() {
    get("/", new IndexRoute(problemSet), new VelocityTemplateEngine());
    post("/submit/:id", new SubmitRoute(testRunner, problemSet),  new VelocityTemplateEngine());
  }
}
