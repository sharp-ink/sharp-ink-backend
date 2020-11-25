package io.sharpink.util;

import java.util.Comparator;

public interface ComparatorUtil {

  /**
   * Utility method to return a "no-op" comparator, which considers all elements to be equal.<br/>
   * Can be used to chain additional comparison operations like <code>thenComparing()</code>.
   */
  static <T> Comparator<T> noOrder() {
    return (obj1, obj2) -> 0;
  }
}
