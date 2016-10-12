package com.boj;
import com.google.common.base.MoreObjects;
import org.apache.commons.io.FileUtils;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import javax.servlet.MultipartConfigElement;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import static spark.Spark.*;

/**
 * Created by biran on 10/10/16.
 */
public class Main {

  private static final ExecutorService testExecutor = Executors.newCachedThreadPool();


  public static void main(String[] args) {
    ProblemSet problemSet = new ProblemSet();
    get("/", (req, res) -> {
      Map<String, Object> model = new HashMap<>();
      model.put("problems", problemSet.getProblems());
      return new ModelAndView(model, "index.html");
    }, new VelocityTemplateEngine());

    post("/submit/:id", (req, resp) -> {
      try {
        int id = Integer.parseInt(req.params(":id"));
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(""));
        String dir = "/tmp/boj/" + id + "/" + System.currentTimeMillis();
        new File(dir).mkdirs();
        try (OutputStream fileWriter = new FileOutputStream(new File(dir + "/Solution.java"));
             InputStream inputStream = req.raw().getPart("source-" + id).getInputStream()) {
          byte[] buf = new byte[1024];
          int count;
          while (true) {
            count = inputStream.read(buf);
            if (count == -1) {
              break;
            }
            fileWriter.write(buf, 0, count);
          }
        }
        try {
          problemSet.getProblem(id).judge(dir);
        } catch (Throwable e) {
          e.printStackTrace();
          return "<pre>" + e.getMessage() + "</pre>";
        }
        return "accepted";
      } catch (Throwable e) {
        e.printStackTrace();
        return "Internal Error";
      }
    });
  }

  public static class ProblemSet {

    private List<Problem> problems = Arrays.asList(
        new Problem(1, "a+b", "print a + b",
            Arrays.asList(
                new TestCase(new Integer[] {1, 2}, 3),
                new TestCase(new Integer[] {-1, 2}, 1))),
        new Problem(2, "gcd", "print gcd of two int",
            Arrays.asList(
                new TestCase(new Integer[] {1, 2}, 1),
                new TestCase(new Integer[] {4, 12}, 4))));

    public List<Problem> getProblems() {
      return problems;
    }

    public Problem getProblem(int id) {
      for (Problem problem : problems) {
        if (problem.getId() == id) {
          return problem;
        }
      }
      return null;
    }
  }

  static class TestCase {
    private final Object[] input;
    private final Object expect;

    public TestCase(Object[] input, Object output) {
      this.input = input;
      this.expect = output;
    }
  }

  public static class Problem {
    private final int id;
    private final String title;
    private final String description;
    private final List<TestCase> tests;

    private Problem(int id,
                    String title,
                    String description,
                    List<TestCase> tests) {
      this.id = id;
      this.title = title;
      this.description = description;
      this.tests = tests;
    }

    public int getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    void judge(String dir) throws Exception {
      build(dir);
      test(dir);
    }

    private void build(String dir) throws IOException, InterruptedException {
      Process process = new ProcessBuilder("javac", dir + "/Solution.java")
          .redirectError(new File(dir + "/err.log"))
          .redirectOutput(new File(dir + "/out.log"))
          .start();
      int returnValue = process.waitFor();
      if (returnValue != 0) {
        throw new CompileError(
            FileUtils.readFileToString(new File(dir + "/err.log"), Charset.defaultCharset()));
      }
    }

    void test(String dir) throws Exception {
      File customElementsDir = new File(dir);
      URL url = customElementsDir.toURI().toURL();
      ClassLoader cl = new URLClassLoader(new URL[] { url });
      Class cls = cl.loadClass("Solution");
      Method method = getMethodByName(cls, "solve");
      testExecutor.submit(() -> {
        for (TestCase testCase : tests) {
          Object solution = null;
          try {
            solution = cls.newInstance();
            Object actual = method.invoke(solution, testCase.input);
            if (!checkEquals(testCase.expect, actual)) {
              throw new WrongAnswer(wrongAnswerMessage(testCase, actual));
            }
          } catch (Throwable e) {
            throw new RuntimeException(e);
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
          .add("Input", testCase.input)
          .add("Expect", testCase.expect)
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

  static class CompileError extends Error {
    public CompileError(String s) {
      super(s);
    }
  }

  static class WrongAnswer extends Error {
    public WrongAnswer(String s) {
      super(s);
    }
  }
}
