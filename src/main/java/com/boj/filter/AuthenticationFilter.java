package com.boj.filter;

import com.boj.base.SessionKeys;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.user.LoginState;
import com.boj.user.UserAuthenticator;
import com.google.inject.Inject;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Session;

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
  public void handle(Request request, Response response) {
    if (SessionKeys.LOGIN_STATE.get(request) != LoginState.OK) {
      String idToken = request.cookie("id_token");
      if (idToken == null) {
        SessionKeys.LOGIN_STATE.set(request, LoginState.NOT_LOGGED_IN);
      } else {
        try {
          UserRecord user = authenticator.authenticateUser(idToken);
          SessionKeys.USER_RECORD.set(request, user);
          SessionKeys.LOGIN_STATE.set(request, LoginState.OK);
        } catch (UserAuthenticator.AuthenticationFailure authenticationFailure) {
          SessionKeys.LOGIN_STATE.set(request, LoginState.GOOGLE_AUTH_FAILED);
        }
      }
    }
  }
}
