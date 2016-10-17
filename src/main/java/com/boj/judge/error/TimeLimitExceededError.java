/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class TimeLimitExceededError extends ErrorWithVerdict {
  private static final long serialVersionUID = -6130808396457318643L;

  public TimeLimitExceededError(String message) {
    super(message, Verdict.TIME_LIMIT_EXCEEDED);
  }
}
