/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class ErrorWithVerdict extends RuntimeException {
  private static final long serialVersionUID = -5890706613194390676L;

  private final Verdict verdict;

  ErrorWithVerdict(String message, Verdict verdict) {
    super(message);
    this.verdict = verdict;
  }

  public Verdict getVerdict() {
    return verdict;
  }
}
