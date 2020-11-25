package io.sharpink.rest.dto.request.story.search;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Criteria {
  private String title;
  private String authorName;
}
