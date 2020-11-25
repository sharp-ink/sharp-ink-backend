package io.sharpink.rest.dto.request.story.search;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorySearch {
  private Criteria criteria;
  private Filter filter;
  private Sort sort;
}
