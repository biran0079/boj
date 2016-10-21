/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.filter;

import com.boj.base.ErrorPage;
import com.boj.jooq.tables.records.UserRecord;
import com.boj.roster.Role;
import com.boj.roster.RosterManager;
import com.boj.user.UserAuthenticator;
import com.boj.user.UserManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Filter;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.TimeUnit;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
@Singleton
public class AccessControlFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);

  private static final ImmutableSet<String> PATH_WHITE_LIST = ImmutableSet.of(
      "/",
      "/error");

  private final Provider<UserRecord> userRecordProvider;
  private final UserManager userManager;
  private final RosterManager rosterManager;

  private final LoadingCache<String, Boolean> userIdToRolwCache = CacheBuilder.newBuilder()
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Boolean>() {
        @Override
        public Boolean load(String userId) throws Exception {
          return rosterManager.getRole(userManager.getUser(userId)) != Role.ALIEN;
        }
      });

  @Inject
  AccessControlFilter(Provider<UserRecord> userRecordProvider,
                      UserManager userManager,
                      RosterManager rosterManager) {
    this.userRecordProvider = userRecordProvider;
    this.userManager = userManager;
    this.rosterManager = rosterManager;
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    if (!canAccessSite(request.pathInfo())) {
      response.redirect(ErrorPage.ACCESS_DENIED.getPath());
    }
  }

  public boolean canAccessSite(String pathInfo) {
    if (PATH_WHITE_LIST.contains(pathInfo)) {
      return true;
    }
    UserRecord userRecord;
    try {
      userRecord = userRecordProvider.get();
      if (userIdToRolwCache.get(userRecord.getId())) {
        return true;
      }
    } catch (Exception e) {
      log.error("Failed to get used record.", e);
    }
    return false;
  }
}
