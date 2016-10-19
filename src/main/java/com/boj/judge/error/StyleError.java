package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * Created by biran on 10/19/16.
 */
public class StyleError extends ErrorWithVerdict {

  public StyleError(String message) {
    super(message, Verdict.STYLE_ERROR);
  }
}
