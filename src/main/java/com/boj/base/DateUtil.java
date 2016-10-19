/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import java.text.SimpleDateFormat;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class DateUtil {

  private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");

  public static int now() {
    return (int) (System.currentTimeMillis() / 1000);
  }

  public static String format(int sec) {
    return FORMAT.format((long) sec * 1000);
  }
}
