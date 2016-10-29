package com.boj.base;

import spark.Request;

/**
 * Created by biran on 10/28/16.
 */
public class SessionKey<T> {

  private final String name;

  SessionKey(String name) {
    this.name = name;
  }

  public T get(Request request) {
    return request.session().attribute(name);
  }

  public void set(Request request, T value) {
    request.session().attribute(name, value);
  }
}
