package io.sharpink.rest.dto.request.story.search;

import io.sharpink.shared.SortType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Sort {
  private SortType title;
  private SortType authorName;
}
