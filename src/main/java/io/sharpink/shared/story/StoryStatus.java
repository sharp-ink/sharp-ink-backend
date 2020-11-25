package io.sharpink.shared.story;

/**
 * Cette Enum représente les différents états possibles pour une histoire
 * (terminée, en cours, en stand-by)
 */
public enum StoryStatus {

  COMPLETE("COMPLETE"), // story is finished, last chapter has been released
  PROGRESS("PROGRESS"), // author is writing it
  STAND_BY("STAND_BY"); // author is taking a pause on this story, he's (temporarily) not writing it anymore

  private String value;

  StoryStatus(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
