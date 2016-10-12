package com.boj;

/**
 * Created by biran on 10/12/16.
 */
public class TestCase {
  private final Object[] input;
  private final Object expect;

  public TestCase(Object[] input, Object output) {
    this.input = input;
    this.expect = output;
  }

  public Object[] getInput() {
    return input;
  }

  public Object getExpect() {
    return expect;
  }
}
