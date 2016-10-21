package com.boj.user;

import com.boj.guice.RequestScope;
import com.boj.jooq.tables.records.UserRecord;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by biran on 10/14/16.
 */
@Singleton
public class UserAuthenticator {

  private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);

  private static final String CLIENT_ID = "496017852143-19a6kjh7mvhlhb0pp4l5fsj5s5empgt8.apps.googleusercontent.com";

  private final LoadingCache<String, UserRecord> idTokenToUserCache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, UserRecord>() {
        @Override
        public UserRecord load(String idTokenString) throws Exception {
          GoogleIdToken idToken = verifier.verify(idTokenString);
          if (idToken == null) {
            log.error("Google authentication failed. for id token {}", idTokenString);
            throw new GoogleAuthException("Google authentication failed.");
          }
          return createOrGetUserFromIdToken(idToken);
        }
      });

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
  private final RequestScope requestScope;

  @Inject
  public UserAuthenticator(UserManager userManager,
                           RequestScope requestScope) {
    this.userManager = userManager;
    this.requestScope = requestScope;
  }

  private UserRecord createOrGetUserFromIdToken(GoogleIdToken idToken) {
    GoogleIdToken.Payload payload = idToken.getPayload();
    UserRecord record = userManager.getUser(payload.getSubject());
    if (record == null) {
      record = userManager.create(payload);
    }
    return record;
  }

  public void authenticateAndSeedUser(String idTokenString) {
    Preconditions.checkNotNull(idTokenString, "idTokenString");
    try {
      seed(idTokenToUserCache.get(idTokenString), LoginState.OK);
    } catch (ExecutionException e) {
      log.error("Google authentication failed.");
      seedGuestUser(LoginState.GOOGLE_AUTH_FAILED);
    }
  }

  public void seedGuestUser(LoginState loginState) {
    seed(userManager.getGuestUser(), loginState);
  }

  private void seed(UserRecord userRecord, LoginState loginState) {
    requestScope.seed(UserRecord.class, userRecord);
    requestScope.seed(LoginState.class, loginState);
  }

  public static class GoogleAuthException extends Exception {
    private static final long serialVersionUID = -1301650153832565472L;

    GoogleAuthException(String message) {
      super(message);
    }
  }
}
