package com.boj;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by biran on 10/14/16.
 */
public class UserAuthenticator {

  private static final String CLIENT_ID = "496017852143-19a6kjh7mvhlhb0pp4l5fsj5s5empgt8.apps.googleusercontent.com";
  private static final ImmutableSet<String> USER_WHITE_LIST = ImmutableSet.of(
      "biran0079@gmail.com");

  private Cache<String, Boolean> authedUser = CacheBuilder.newBuilder()
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

  boolean canUse(String idTokenString) throws GeneralSecurityException, IOException {
    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken != null) {
      if (authedUser.getIfPresent(idToken) != null) {
        return true;
      }
      GoogleIdToken.Payload payload = idToken.getPayload();
      if (USER_WHITE_LIST.contains(payload.getEmail())) {
        authedUser.put(idTokenString, true);
        return true;
      }
    }
    return false;
  }
}
