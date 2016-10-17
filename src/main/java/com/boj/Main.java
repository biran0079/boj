package com.boj;

import com.google.inject.Guice;

/**
 * Created by biran on 10/10/16.
 */
public class Main {

  public static void main(String[] args) {
    Guice.createInjector(new BojServerModule()).getInstance(BojServer.class).start();
  }
}
