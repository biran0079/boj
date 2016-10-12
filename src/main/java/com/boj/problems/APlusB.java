package com.boj.problems;

import com.boj.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by biran on 10/12/16.
 */
public class APlusB implements Problem {

  @Override
  public String getTitle() {
    return "a+b";
  }

  @Override
  public String getDescription() {
    return "compute a + b";
  }

  @Override
  public List<TestCase> getTests() {
    return Arrays.asList(
        new TestCase(new Integer[]{1, 2}, 3),
        new TestCase(new Integer[]{-1, 2}, 1));
  }
}
