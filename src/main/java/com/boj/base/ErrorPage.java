/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public enum ErrorPage {

  NO_PERMISSION("You don't have permission to access this page."),
  ACCESS_DENIED("You don't have access to this site. Please reach out to biran0079@gmail.com for access."),
  UNKNOWN("Unknown error occurred.");

  private static final Logger log = LoggerFactory.getLogger(ErrorPage.class);

  private final String message;

  ErrorPage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public String getPath() {
    return "/error?reason=" + name();
  }

  public static ErrorPage fromString(String str) {
    try {
      return ErrorPage.valueOf(str);
    } catch (Throwable e) {
      log.error("Unknown error reason: {}", str, e);
      return UNKNOWN;
    }
  }
}
