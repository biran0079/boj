package com.boj;

import com.boj.problems.ProblemSet;
import spark.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by biran on 10/14/16.
 */
public class ProblemsRoute implements TemplateViewRoute {

  private final ProblemSet problemSet;

  public ProblemsRoute(ProblemSet problemSet) {
    this.problemSet = problemSet;
  }

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    Map<String, Object> model = new HashMap<>();
    model.put("problemSet", problemSet);
    return new ModelAndView(model, "problems.html");
  }
}
