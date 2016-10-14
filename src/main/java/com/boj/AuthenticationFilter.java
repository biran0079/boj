package com.boj;

import com.google.common.collect.ImmutableSet;
import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Created by biran on 10/14/16.
 */
public class AuthenticationFilter implements Filter {

  private static final ImmutableSet<String> PATH_WHITE_LIST = ImmutableSet.of("/", "/signout");

  private final UserAuthenticator authenticator = new UserAuthenticator();

  @Override
  public void handle(Request request, Response response) throws Exception {
    if (PATH_WHITE_LIST.contains(request.pathInfo())) {
      return;
    }
    String idToken = request.cookie("id_token");
    if (idToken == null || !authenticator.canUse(idToken)) {
      response.removeCookie("id_token");
      response.redirect("/");
    }
  }
}
