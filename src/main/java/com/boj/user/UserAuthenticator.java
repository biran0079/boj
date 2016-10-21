package com.boj.user;

import com.boj.guice.RequestScope;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.roster.RosterManager;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import javax.inject.Singleton;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by biran on 10/14/16.
 */
@Singleton
public class UserAuthenticator {

  private static final String CLIENT_ID = "496017852143-19a6kjh7mvhlhb0pp4l5fsj5s5empgt8.apps.googleusercontent.com";

  private Cache<String, UserRecord> authedUser = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

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
  private final RosterManager rosterManager;

  @Inject
  public UserAuthenticator(UserManager userManager,
                           RequestScope requestScope,
                           RosterManager rosterManager) {
    this.userManager = userManager;
    this.requestScope = requestScope;
    this.rosterManager = rosterManager;
  }

  public boolean canUse(String idTokenString) throws GeneralSecurityException, IOException {
    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken != null) {
      if (authedUser.getIfPresent(idToken) != null) {
        return true;
      }
      GoogleIdToken.Payload payload = idToken.getPayload();
      if (rosterManager.isInRoster(payload.getEmail())) {
        UserRecord record = userManager.getUser(payload.getSubject());
        if (record == null) {
          record = userManager.create(payload);
        }
        requestScope.seed(UserRecord.class, record);
        authedUser.put(idTokenString, record);
        return true;
      }
    }
    return false;
  }
}
