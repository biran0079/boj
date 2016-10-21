/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import com.boj.BojServer;
import com.boj.jooq.tables.records.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class PermissionDeniedExceptionHandler implements ExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(BojServer.class);

  private final Provider<UserRecord> userRecordProvider;

  @Inject
  PermissionDeniedExceptionHandler(Provider<UserRecord> userRecordProvider) {
    this.userRecordProvider = userRecordProvider;
  }

  @Override
  public void handle(Exception e, Request request, Response response) {
    logger.info("User {} does not have permission for request {}.",
        userRecordProvider.get().getEmail(),
        request.pathInfo());
    response.redirect(ErrorPage.NO_PERMISSION.getPath());
  }
}
