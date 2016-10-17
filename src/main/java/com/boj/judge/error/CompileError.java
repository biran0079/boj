package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * Created by biran on 10/12/16.
 */
public class CompileError extends ErrorWithVerdict {
  private static final long serialVersionUID = -3017722378790151636L;

  public CompileError(String s) {
    super(s, Verdict.COMPILE_ERROR);
  }
}
