package com.boj.guice;

import com.google.common.collect.Maps;
import com.google.inject.*;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author Ran Bi (ran.bi@addepar.com)
 */
public class RequestScope implements Scope {

  private final ThreadLocal<Map<Key<?>, Object>> values = new ThreadLocal<>();

  public void enter() {
    // checkState(values.get() == null, "A scoping block is already in progress");
    if (values.get() != null) {
      exit();
    }
    values.set(Maps.newHashMap());
  }

  public void exit() {
    checkState(values.get() != null, "No scoping block in progress");
    values.remove();
  }

  public <T> void seed(Key<T> key, T value) {
    Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
    checkState(!scopedObjects.containsKey(key), "A value for the key %s was "
            + "already seeded in this scope. Old value: %s New value: %s", key,
        scopedObjects.get(key), value);
    scopedObjects.put(key, value);
  }

  public <T> void seed(Class<T> clazz, T value) {
    seed(Key.get(clazz), value);
  }

  public boolean isSeeded(Class<?> clazz) {
    Key<?> key = Key.get(clazz);
    return getScopedObjectMap(key).containsKey(key);
  }

  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    return () -> {
      Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

      @SuppressWarnings("unchecked")
      T current = (T) scopedObjects.get(key);
      if (current == null && !scopedObjects.containsKey(key)) {
        current = unscoped.get();

        // don't remember proxies; these exist only to serve circular dependencies
        if (Scopes.isCircularProxy(current)) {
          return current;
        }

        scopedObjects.put(key, current);
      }
      return current;
    };
  }

  private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
    Map<Key<?>, Object> scopedObjects = values.get();
    if (scopedObjects == null) {
      throw new OutOfScopeException("Cannot access " + key
          + " outside of a scoping block");
    }
    return scopedObjects;
  }
}
