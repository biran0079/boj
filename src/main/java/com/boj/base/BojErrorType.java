/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import spark.Response;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public enum BojErrorType {

  NO_PERMISSION("You don't have permission to access this page."),
  ACCESS_DENIED("You don't have access to this site. Please reach out to biran0079@gmail.com for access."),
  PROBLEM_NOT_EXIST("/problems", "Problem does not exist."),
  SUBMISSION_NOT_EXIST("/submits", "Submission does not exist."),
  UNKNOWN("Unknown error occurred.");

  private final String baseUrl;
  private final String message;

  BojErrorType(String message) {
    this("/", message);
  }
  BojErrorType(String baseUrl, String message) {
    this.baseUrl = baseUrl;
    this.message = message;
  }

  public String getPath() {
    return baseUrl + "?error=" + message;
  }

  public void handle(Response response) {
    response.redirect(getPath());
  }

  public BojException exception() {
    throw new BojException(this);
  }
}
