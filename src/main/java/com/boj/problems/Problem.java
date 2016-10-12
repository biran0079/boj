package com.boj.problems;

import com.boj.TestCase;

import java.util.List;

/**
 * Created by biran on 10/12/16.
 */
public interface Problem {

  String getTitle();

  String getDescription();

  List<TestCase> getTests();
}
