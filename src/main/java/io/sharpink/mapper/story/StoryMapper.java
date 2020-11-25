package io.sharpink.mapper.story;

import io.sharpink.persistence.entity.story.ChaptersLoadingStrategy;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.rest.dto.request.story.StoryRequest;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.shared.story.StoryStatus;
import io.sharpink.shared.story.StoryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

// @formatter:off
@Component
public class StoryMapper {

  private final ChapterMapper chapterMapper;

  @Autowired
  public StoryMapper(ChapterMapper chapterMapper) {
    this.chapterMapper = chapterMapper;
  }

  public StoryResponse toStoryResponse(Story source, ChaptersLoadingStrategy chaptersLoadingStrategy) {
    StoryResponse target = StoryResponse.builder()
      .id(source.getId())
      .title(source.getTitle())
      .type(source.getType() != null ? source.getType().value() : null)
      .status(source.getStatus() != null ? source.getStatus().value() : null)
      .summary(source.getSummary())
      .thumbnail(source.getThumbnail())
      .published(source.isPublished())
      .chaptersNumber(source.getChaptersNumber())
      .originalStory(source.isOriginalStory())
      .authorId(source.getAuthor().getId())
      .creationDate(source.getCreationDate())
      .lastModificationDate(source.getLastModificationDate())
      .finalReleaseDate(source.getFinalReleaseDate())
      .threadId(source.getThread() != null ? source.getThread().getId() : null)
			.build();

		if (chaptersLoadingStrategy == ChaptersLoadingStrategy.ALL) {
			target.setChapters(chapterMapper.toChapterResponseList(source.getChapters()));
		} else if (chaptersLoadingStrategy == ChaptersLoadingStrategy.ONLY_FIRST && source.getChaptersNumber() > 0) {
		  target.setChapters(chapterMapper.toChapterResponseList(source.getChapters().subList(0, 1)));
    }

		return target;
	}

	public List<StoryResponse> toStoryResponseList(List<Story> source, ChaptersLoadingStrategy chaptersLoadingStrategy) {
		return source.stream().map(story -> toStoryResponse(story, chaptersLoadingStrategy)).collect(toList());
	}

	public Story toStory(StoryRequest source) {
    return Story.builder()
      .title(source.getTitle())
      .type(source.getType() != null ? StoryType.valueOf(source.getType()) : StoryType.UNDETERMINED)
      .originalStory(source.isOriginalStory())
      .status(source.getStatus() != null ? StoryStatus.valueOf(source.getStatus()) : null)
      .summary(source.getSummary())
      .published(source.isPublished())
      .build();
	}
}
