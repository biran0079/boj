package com.boj.filter;

import com.boj.user.UserAuthenticator;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import spark.Filter;
import spark.Request;
import spark.Response;

import javax.inject.Singleton;

/**
 * Created by biran on 10/14/16.
 */
@Singleton
public class AuthenticationFilter implements Filter {

  private static final ImmutableSet<String> PATH_WHITE_LIST = ImmutableSet.of("/");

  private final UserAuthenticator authenticator;

  @Inject
  public AuthenticationFilter(UserAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

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
