package com.boj.problems;

import com.boj.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by biran on 10/12/16.
 */
public class GCD implements Problem {

  @Override
  public String getTitle() {
    return "GCD";
  }

  @Override
  public String getDescription() {
    return "Compute the greatest common divisor of 2 numbers.";
  }

  @Override
  public List<TestCase> getTests() {
    return Arrays.asList(
        new TestCase(new Integer[]{1, 2}, 1),
        new TestCase(new Integer[]{4, 12}, 4));
  }
}
