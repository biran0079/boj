/*
 * Copyright 2016 Addepar. All rights reserved.
 */
package com.boj.base;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class MapBuilder {

  private final Map<String, Object> map = new HashMap<>();

  private MapBuilder() {
  }

  public static MapBuilder create() {
    return new MapBuilder();
  }

  public MapBuilder put(String key, Object value) {
    map.put(key, value);
    return this;
  }

  public Map<String, Object> build() {
    return map;
  }
}
