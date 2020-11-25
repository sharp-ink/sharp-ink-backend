package io.sharpink.rest.dto.request.story;

import io.sharpink.shared.story.StoryStatus;
import io.sharpink.shared.story.StoryType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StoryPatchRequest {
  private String title;
  private boolean originalStory;
  private StoryStatus status;
  private StoryType type;
  private String summary;
  private String thumbnail;
  private Boolean published;
}
