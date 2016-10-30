/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import javax.inject.Inject;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class BojExceptionHandler implements ExceptionHandler {

  @Inject
  BojExceptionHandler() {
  }

  @Override
  public void handle(Exception e, Request request, Response response) {
    BojException bojException = (BojException) e;
    bojException.getBojErrorType().handle(response);
  }
}
