/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class RuntimeError extends ErrorWithVerdict {
  private static final long serialVersionUID = 6401186689737420993L;

  public RuntimeError(String message) {
    super(message, Verdict.RUNTIME_ERROR);
  }
}
