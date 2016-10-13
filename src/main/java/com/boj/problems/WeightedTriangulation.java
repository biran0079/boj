/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.problems;

import com.boj.TestCase;

import java.util.List;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class WeightedTriangulation implements Problem {

  @Override
  public String getTitle() {
    return "weighted triangulation";
  }

  @Override
  public String getDescription() {
    return "Given convex polygon of n vertices, each vertex has a value associated with it. " +
        "Find the triangulation that maximize the sum of product of weights for each triangle.";
  }

  @Override
  public List<TestCase> getTests() {
    return null;
  }
}
