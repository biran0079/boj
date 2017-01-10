package com.boj.judge;

import com.boj.annotation.CheckstyleConfigPath;
import com.boj.annotation.CheckstyleJarPath;
import com.boj.annotation.JunitClassPath;
import com.boj.jooq.tables.records.ProblemRecord;
import com.boj.jooq.tables.records.SubmissionRecord;
import com.boj.judge.error.*;
import com.boj.problem.ProblemManager;
import com.boj.problem.ProblemSolvedListener;
import com.boj.submission.SubmissionManager;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by biran on 10/12/16.
 */
@Singleton
public class Judge {

  private static final Pattern CLASS_NAME_PATTERN =
      Pattern.compile("^(?:public +)?(?:final +)?class +([a-zA-Z0-9]+)", Pattern.MULTILINE);

  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final ProblemManager problemManager;
  private final SubmissionManager submissionManager;
  private final String junitClassPath;
  private final String checkStyleJar;
  private final String checkStyleConfig;
  private final Set<ProblemSolvedListener> problemSolvedListeners;

  @Inject
  Judge(ProblemManager problemManager,
        SubmissionManager submissionManager,
        @JunitClassPath String junitClassPath,
        @CheckstyleJarPath String checkStyleJar,
        @CheckstyleConfigPath String checkStyleConfig,
        Set<ProblemSolvedListener> problemSolvedListeners) {
    this.problemManager = problemManager;
    this.submissionManager = submissionManager;
    this.junitClassPath = junitClassPath;
    this.checkStyleJar = checkStyleJar;
    this.checkStyleConfig = checkStyleConfig;
    this.problemSolvedListeners = problemSolvedListeners;
  }

  public void submit(SubmissionRecord submission) {
    executor.submit(() -> judge(problemManager.getProblemById(submission.getProblemId()), submission));
  }

  private void judge(ProblemRecord problem, SubmissionRecord submission) {
    try {
      File dir = new File("/tmp/boj/" + submission.getId());
      dir.mkdirs();
      String solutionSrc = submission.getSubmittedSrc();
      String unitTestSrc = problem.getJunitTestSrc();
      String unitTestClass = extractClassName(unitTestSrc);
      String solutionClass = extractClassName(solutionSrc);
      if (unitTestClass == null) {
        submissionManager.updateVerdictAndMessage(submission.getId(), Verdict.INTERNAL_ERROR,
            "Cannot find unit test class name.");
        return;
      }
      if (solutionClass == null) {
        submissionManager.updateVerdictAndMessage(submission.getId(), Verdict.SRC_FORMAT_ERROR,
            "Cannot find class name in submitted code.");
        return;
      }
      FileUtils.write(new File(dir, solutionClass + ".java"), solutionSrc, Charset.defaultCharset());
      checkStyke(dir.getAbsolutePath(), solutionClass);
      FileUtils.write(new File(dir, unitTestClass + ".java"), unitTestSrc, Charset.defaultCharset());
      compile(dir.getAbsolutePath(), solutionClass);
      compile(dir.getAbsolutePath(), unitTestClass);
      run(dir.getAbsolutePath(), unitTestClass);
      submissionManager.updateVerdictAndMessage(submission.getId(), Verdict.ACCEPTED, "");
      problemSolvedListeners.forEach(
          listener -> listener.onProblemSolved(problem.getId(), submission.getUserId()));
    } catch (ErrorWithVerdict e) {
      submissionManager.updateVerdictAndMessage(submission.getId(), e.getVerdict(), e.getMessage());
    } catch (Throwable e) {
      submissionManager.updateVerdictAndMessage(submission.getId(), Verdict.INTERNAL_ERROR, e.getMessage());
    }
  }

  private String extractClassName(String src) {
    Matcher matcher = CLASS_NAME_PATTERN.matcher(src);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  private void run(String dir, String className) throws IOException, InterruptedException {
    String classPath = dir + ":" + junitClassPath;
    Process process = new ProcessBuilder("java", "-Xmx64m", "-cp", classPath,
        "org.junit.runner.JUnitCore", className)
        .directory(new File(dir))
        .redirectError(new File(dir + "/run-err.log"))
        .redirectOutput(new File(dir + "/run-out.log"))
        .start();
    int returnValue = process.waitFor();

    if (returnValue != 0) {
      String message = FileUtils.readFileToString(new File(dir + "/run-out.log"), Charset.defaultCharset());
      if (message.contains("java.lang.AssertionError") || message.contains("org.junit.ComparisonFailure")) {
        throw new WrongAnswer(message);
      } else if (message.contains("org.junit.runners.model.TestTimedOutException")) {
        throw new TimeLimitExceededError(message);
      } else if (message.contains("java.lang.OutOfMemoryError")) {
        throw new MemoryLimitExceededError(message);
      } else {
        throw new RuntimeError(message);
      }
    }
  }

  private void compile(String dir, String className) throws IOException, InterruptedException {
    String classPath = dir + ":" + junitClassPath;
    Process process = new ProcessBuilder("javac", "-cp", classPath, dir + "/" + className + ".java")
        .directory(new File(dir))
        .redirectError(new File(dir + "/compile-err.log"))
        .redirectOutput(new File(dir + "/compile-out.log"))
        .start();
    int returnValue = process.waitFor();
    if (returnValue != 0) {
      throw new CompileError(
          FileUtils.readFileToString(new File(dir + "/compile-err.log"), Charset.defaultCharset()));
    }
  }

  private void checkStyke(String dir, String className) throws IOException, InterruptedException {
    File output = new File(dir + "/checkstyle-out.log");
    File error = new File(dir + "/checkstyle-err.log");
    Process process = new ProcessBuilder("java", "-jar", checkStyleJar, "-c", checkStyleConfig,
        dir + "/" + className + ".java")
        .directory(new File(dir))
        .redirectError(error)
        .redirectOutput(output)
        .start();
    process.waitFor();
    String checkstyleOutput = FileUtils.readFileToString(output, Charset.defaultCharset());
    if (checkstyleOutput.contains("[WARN]")) {
      throw new StyleError(checkstyleOutput);
    }
  }
}
