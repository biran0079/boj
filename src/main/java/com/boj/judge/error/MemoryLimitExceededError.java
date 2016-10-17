/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class MemoryLimitExceededError extends ErrorWithVerdict {
  private static final long serialVersionUID = -8827312941669695085L;

  public MemoryLimitExceededError(String message) {
    super(message, Verdict.MEMORY_LIMIT_EXCEEDED);
  }
}
