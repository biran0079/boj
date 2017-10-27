package com.boj.user;

import com.boj.jooq.tables.records.UserRecord;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Created by biran on 10/14/16.
 */
@Singleton
public class UserAuthenticator {

  private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);
  private static final String CLIENT_ID = "1026335537092-3oqpj8tck2lqu7ts0vism5jts61v4ii6.apps.googleusercontent.com";

  private final GoogleIdTokenVerifier verifier =
      new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), new JacksonFactory())
          .setAudience(Arrays.asList(CLIENT_ID))
          // If you retrieved the token on Android using the Play Services 8.3 API or newer, set
          // the issuer to "https://accounts.google.com". Otherwise, set the issuer to
          // "accounts.google.com". If you need to verify tokens from multiple sources, build
          // a GoogleIdTokenVerifier for each issuer and try them both.
          .setIssuer("accounts.google.com")
          .build();

  private final UserManager userManager;

  @Inject
  public UserAuthenticator(UserManager userManager) {
    this.userManager = userManager;
  }

  private UserRecord createOrGetUserFromIdToken(GoogleIdToken idToken) {
    GoogleIdToken.Payload payload = idToken.getPayload();
    UserRecord record = userManager.getUser(payload.getSubject());
    if (record == null) {
      record = userManager.create(payload);
    }
    return record;
  }

  public UserRecord authenticateUser(String idTokenString) throws AuthenticationFailure {
    Preconditions.checkNotNull(idTokenString, "idTokenString");
    GoogleIdToken idToken;
    try {
      idToken = verifier.verify(idTokenString);
    } catch (GeneralSecurityException|IOException e) {
      log.error("Google authentication failed for id token {}", idTokenString);
      throw new AuthenticationFailure(e.getMessage());
    }
    if (idToken == null) {
      log.error("Google authentication failed for id token {}", idTokenString);
      throw new AuthenticationFailure("Google authentication failed.");
    }
    return createOrGetUserFromIdToken(idToken);
  }

  public static class AuthenticationFailure extends Exception {
    private static final long serialVersionUID = -1301650153832565472L;

    AuthenticationFailure(String message) {
      super(message);
    }
  }
}
