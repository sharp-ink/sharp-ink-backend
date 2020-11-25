package io.sharpink.shared.story;

/**
 * Cette Enum représente le genre de l'histoire (fantasy, science fiction,
 * biographie,...)
 */
public enum StoryType {

  FANTASY("FANTASY"),
  SCI_FI("SCI_FI"),
  BIOGRAPHY("BIOGRAPHY"),
  ADVENTURE("ADVENTURE"),
  ROMANCE("ROMANCE"),
  UNDETERMINED("UNDETERMINED");

  private String value;

  StoryType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
