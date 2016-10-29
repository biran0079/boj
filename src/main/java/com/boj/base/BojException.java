package com.boj.base;

/**
 * Created by biran on 10/29/16.
 */
public class BojException extends RuntimeException {

  private final BojErrorType bojErrorType;

  public BojException(BojErrorType bojErrorType) {
    this.bojErrorType = bojErrorType;
  }

  public BojErrorType getBojErrorType() {
    return bojErrorType;
  }
}
