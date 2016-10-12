package com.boj;

import com.boj.problems.Problem;
import com.boj.problems.ProblemSet;
import com.google.common.base.Strings;
import org.apache.commons.io.FileUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

import javax.servlet.MultipartConfigElement;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by biran on 10/12/16.
 */
public class SubmitRoute implements TemplateViewRoute {

  private final TestRunner testRunner;
  private final ProblemSet problemSet;

  public SubmitRoute(TestRunner testRunner, ProblemSet problemSet) {
    this.testRunner = testRunner;
    this.problemSet = problemSet;
  }

  @Override
  public ModelAndView handle(Request req, Response resp) throws Exception {
    Map<String, Object> model = new HashMap<>();
    try {
      int id = Integer.parseInt(req.params(":id"));
      String name = req.queryParams("name");
      if (Strings.isNullOrEmpty(name)) {
        throw new RuntimeException("name should not be empty");
      }
      req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(""));
      String dir = "/tmp/boj/" + name + "/" + id + "/" + System.currentTimeMillis();
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
        judge(problemSet.getProblem(id), dir);
        model.put("message", "accepted");
      } catch (TimeoutException e) {
        model.put("message", "time limit exceeded");
      } catch (Throwable e) {
        model.put("message", e.getMessage());
      }
    } catch (Throwable e) {
      e.printStackTrace();
      model.put("message", e.getMessage());
    }
    return new ModelAndView(model, "result.html");
  }

  void judge(Problem problem, String dir) throws Exception {
    build(dir);
    testRunner.test(problem, dir);
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
}
