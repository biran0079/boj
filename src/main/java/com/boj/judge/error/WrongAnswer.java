package com.boj.judge.error;

import com.boj.judge.Verdict;

/**
 * Created by biran on 10/12/16.
 */
public class WrongAnswer extends ErrorWithVerdict {
  private static final long serialVersionUID = 4666973361676331699L;

  public WrongAnswer(String s) {
    super(s, Verdict.WRONG_ANSWER);
  }
}
