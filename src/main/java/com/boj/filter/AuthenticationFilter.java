package com.boj.filter;

import com.boj.base.ErrorPage;
import com.boj.user.UserAuthenticator;
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

  private final UserAuthenticator authenticator;

  @Inject
  public AuthenticationFilter(UserAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    String idToken = request.cookie("id_token");
    if (idToken == null) {
      authenticator.seedGuestUser();
    } else {
      authenticator.authenticateAndSeedUser(idToken);
    }
  }
}
