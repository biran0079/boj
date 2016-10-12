package com.boj;

import com.boj.problems.ProblemSet;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by biran on 10/12/16.
 */
public class IndexRoute implements TemplateViewRoute {

  private final ProblemSet problemSet;

  public IndexRoute(ProblemSet problemSet) {
    this.problemSet = problemSet;
  }

  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    Map<String, Object> model = new HashMap<>();
    model.put("problemSet", problemSet);
    return new ModelAndView(model, "index.html");
  }
}
