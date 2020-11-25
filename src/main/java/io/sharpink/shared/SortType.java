package io.sharpink.shared;


public enum SortType {
  NONE, ASC, DESC;

  public static boolean isDefined(SortType sortType) {
    return sortType != null && !sortType.equals(NONE);
  }
}
