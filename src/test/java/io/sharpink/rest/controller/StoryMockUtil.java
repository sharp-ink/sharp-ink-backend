package io.sharpink.rest.controller;

import io.sharpink.UserMockUtil;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.shared.story.StoryStatus;
import io.sharpink.shared.story.StoryType;

import static java.util.Collections.emptyList;

public class StoryMockUtil {

  static final Story STORY_MOCK = mockStory();
  static final StoryResponse STORY_RESPONSE_MOCK = mockStoryResponse();

  public static Story getStoryMock() {
    return STORY_MOCK;
  }

  public static StoryResponse getStoryResponseMock() {
    return STORY_RESPONSE_MOCK;
  }

  private static Story mockStory() {
    return Story.builder()
      .id(1L)
      .title("Hello world!")
      .type(StoryType.UNDETERMINED)
      .status(StoryStatus.PROGRESS)
      .chaptersNumber(0)
      .author(UserMockUtil.getUserMock())
      .chapters(emptyList())
      .build();
  }

  private static StoryResponse mockStoryResponse() {
    return null; // TODO
  }

}
