package com.boj;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import java.util.HashMap;

/**
 * Created by biran on 10/12/16.
 */
public class IndexRoute implements TemplateViewRoute {


  @Override
  public ModelAndView handle(Request request, Response response) throws Exception {
    return new ModelAndView(new HashMap<>(), "index.html");
  }
}
