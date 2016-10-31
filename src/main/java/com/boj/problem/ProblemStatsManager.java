package com.boj.problem;

import com.boj.submission.SubmissionManager;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;

import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;

/**
 * Created by biran on 10/31/16.
 */
@Singleton
public class ProblemStatsManager implements ProblemSolvedListener {

  private final LoadingCache<Integer, ProblemStats> statsCache = CacheBuilder.newBuilder()
      .build(new CacheLoader<Integer, ProblemStats>() {
        @Override
        public ProblemStats load(Integer problemId) {
          return loadProblemStats(problemId);
        }
      });

  private final SubmissionManager submissionManager;

  @Inject
  public ProblemStatsManager(SubmissionManager submissionManager) {
    this.submissionManager = submissionManager;
  }

  public ProblemStats getProblemStat(int problemId) {
    return statsCache.getUnchecked(problemId);
  }

  private ProblemStats loadProblemStats(int problemId) {
    return new ProblemStats(problemId,
        submissionManager.getUserIdsWhoSolved(problemId));
  }

  @Override
  public void onProblemSolved(int problemId, String userId) {
    if (!statsCache.getUnchecked(problemId).isSolvedBy(userId)) {
      statsCache.refresh(problemId);
    }
  }
}
