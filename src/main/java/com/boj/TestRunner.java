package com.boj;

import com.boj.problems.Problem;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by biran on 10/12/16.
 */
public class TestRunner {

  private static final ExecutorService testExecutor = Executors.newCachedThreadPool();

  void test(Problem problem, String classDir) throws Exception {
    File customElementsDir = new File(classDir);
    URL url = customElementsDir.toURI().toURL();
    ClassLoader cl = new URLClassLoader(new URL[]{url});
    Class cls = cl.loadClass("Solution");
    Method method = getMethodByName(cls, "solve");
    testExecutor.submit(() -> {
      for (TestCase testCase : problem.getTests()) {
        Object solution = null;
        try {
          solution = cls.newInstance();
          Object actual = method.invoke(solution, testCase.getInput());
          if (!checkEquals(testCase.getExpect(), actual)) {
            throw new WrongAnswer(wrongAnswerMessage(testCase, actual));
          }
        } catch (Throwable e) {
          throw new RuntimeException(e.getMessage());
        }
      }
    }).get(1000, TimeUnit.MILLISECONDS);
  }

  private boolean checkEquals(Object expect, Object actual) {
    if (expect.getClass().isArray()) {
      return Arrays.deepEquals((Object[]) expect, (Object[]) actual);
    }
    return expect.equals(actual);
  }

  private String wrongAnswerMessage(TestCase testCase, Object actual) {
    return MoreObjects.toStringHelper(testCase)
        .add("Input", testCase.getInput())
        .add("Expect", testCase.getExpect())
        .add("Actual", actual)
        .toString();
  }

  private Method getMethodByName(Class cls, String name) {
    for (Method method : cls.getMethods()) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    throw new IllegalArgumentException("Method " + name + " not found.");
  }
}
