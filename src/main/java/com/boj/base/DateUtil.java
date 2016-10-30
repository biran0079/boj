package com.boj.base;

import java.text.SimpleDateFormat;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class DateUtil {

  private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");

  private static final long ONE_MINUTE_MS = 60L * 1000L;
  private static final long ONE_HOUR_MS = 60L * ONE_MINUTE_MS;
  private static final long ONE_DAY_MS = 24L * ONE_HOUR_MS;
  private static final long ONE_WEEK_MS = 7L * ONE_DAY_MS;

  public static int now() {
    return (int) (System.currentTimeMillis() / 1000);
  }

  public static String format(int sec) {
    long now = System.currentTimeMillis();
    long then = (long) sec * 1000;
    if (now - then < ONE_MINUTE_MS) {
      return "just now";
    }
    if (now - then < ONE_HOUR_MS) {
      return "a few minutes ago";
    }
    if (now - then < ONE_DAY_MS) {
      return "a few hours ago";
    }
    if (now - then < ONE_WEEK_MS) {
      return "a few days ago";
    }
    return FORMAT.format(then);
  }
}
