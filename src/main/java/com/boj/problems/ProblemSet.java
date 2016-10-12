package com.boj.problems;

import com.boj.problems.Problem;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by biran on 10/12/16.
 */
public class ProblemSet {

  private static int ID_COUNTER = 0;
  private final ImmutableBiMap<Integer, Problem> idToProblem;

  ProblemSet(Problem... problems) {
    ImmutableBiMap.Builder<Integer, Problem> builder = ImmutableBiMap.builder();
    for (Problem problem : problems) {
      builder.put(ID_COUNTER++, problem);
    }
    idToProblem = builder.build();
  }

  public Collection<Problem> getProblems() {
    return idToProblem.values();
  }

  public Problem getProblem(int id) {
    return idToProblem.get(id);
  }

  public int getId(Problem problem) {
    return idToProblem.inverse().get(problem);
  }
}
