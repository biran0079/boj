/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.Solution;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class WeightedTriangulationSolution {

  int solve(int[] arr) {
    // dp[i][j]: the max sum of production of weights of triangulation for arr[i...j]
    int[][] dp = new int[arr.length][arr.length];
    for (int n = 3; n <= arr.length; n++) {
      for (int i = 0; i <= arr.length - n; i++) {
        int j = i + n - 1;
        for (int k = i + 1; k < j; k++) {
          dp[i][j] = Math.max(dp[i][j], dp[i][k] + dp[k][j] + arr[i] * arr[j] * arr[k]);
        }
      }
    }
    return dp[0][arr.length - 1];
  }
}
