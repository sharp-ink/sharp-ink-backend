package io.sharpink.service;

import io.sharpink.UserMockUtil;
import io.sharpink.mapper.story.ChapterMapper;
import io.sharpink.mapper.story.StoryMapper;
import io.sharpink.persistence.dao.story.ChapterDao;
import io.sharpink.persistence.dao.story.StoryDao;
import io.sharpink.persistence.dao.user.UserDao;
import io.sharpink.persistence.entity.story.ChaptersLoadingStrategy;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.persistence.entity.user.User;
import io.sharpink.rest.controller.StoryMockUtil;
import io.sharpink.rest.dto.request.story.search.Criteria;
import io.sharpink.rest.dto.request.story.search.Sort;
import io.sharpink.rest.dto.request.story.search.StorySearch;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.service.picture.PictureManagementService;
import io.sharpink.shared.SortType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

  @Mock UserDao userDaoMock;
  @Mock StoryDao storyDaoMock;
  @Mock ChapterDao chapterDaoMock;
  ChapterMapper chapterMapper = new ChapterMapper();
  StoryMapper storyMapper = new StoryMapper(chapterMapper);
  @Mock PictureManagementService pictureManagementServiceMock;

  @InjectMocks
  @Spy
  StoryService storyService = new StoryService(userDaoMock, storyDaoMock, chapterDaoMock, storyMapper, chapterMapper, pictureManagementServiceMock);

  @Test
  void searchStories_BasicSearchWithNoSorting() {
    // given
    Story storyMock = StoryMockUtil.getStoryMock();
    when(storyDaoMock.findAll(any(Specification.class))).thenReturn(singletonList(storyMock));

    // when
    StorySearch storySearch = StorySearch.builder()
      .criteria(Criteria.builder().title(randomAlphabetic(10)).authorName(randomAlphabetic(10)).build())
      .build();
    List<StoryResponse> storyResponseList = storyService.searchStories(storySearch);

    // then
    verify(storyDaoMock).findAll(any(Specification.class));
    verify(storyService, never()).applySorting(anyList(), any(Sort.class));
    assertThat(storyResponseList.size()).isEqualTo(1);
    StoryResponse expectedStoryResponse = storyMapper.toStoryResponse(storyMock, ChaptersLoadingStrategy.ONLY_FIRST);
    assertThat(storyResponseList.get(0)).isEqualTo(expectedStoryResponse);
  }

  @Test
  void searchStories_BasicSearchSortByTitleAsc() {
    // given
    User userMock = UserMockUtil.getUserMock();
    List<Story> storyListMock = asList(
      Story.builder().id(1L).title("A Beautiful Crate").author(userMock).chaptersNumber(0).build(),
      Story.builder().id(2L).title("Great Holiday Inn").author(userMock).chaptersNumber(0).build(),
      Story.builder().id(3L).title("Don't Exist Forever!").author(userMock).chaptersNumber(0).build()
    );
    when(storyDaoMock.findAll(any(Specification.class))).thenReturn(storyListMock);

    // when
    Criteria criteria = Criteria.builder().title(randomAlphabetic(10)).authorName(randomAlphabetic(10)).build();
    Sort sort = Sort.builder().title(SortType.ASC).build();
    StorySearch storySearch = StorySearch.builder().criteria(criteria).sort(sort).build();
    List<StoryResponse> storyResponseList = storyService.searchStories(storySearch);

    // then
    verify(storyDaoMock).findAll(any(Specification.class));
    verify(storyService).applySorting(storyListMock, sort);
    assertThat(storyResponseList.size()).isEqualTo(3);
    assertThat(storyResponseList).isSortedAccordingTo(comparing(StoryResponse::getTitle));
  }

  @Test
  void searchStories_BasicSearchSortByTitleDesc() {
    // given
    User userMock = UserMockUtil.getUserMock();
    List<Story> storyListMock = asList(
      Story.builder().id(1L).title("A Beautiful Crate").author(userMock).chaptersNumber(0).build(),
      Story.builder().id(2L).title("Great Holiday Inn").author(userMock).chaptersNumber(0).build(),
      Story.builder().id(3L).title("Don't Exist Forever!").author(userMock).chaptersNumber(0).build()
    );
    when(storyDaoMock.findAll(any(Specification.class))).thenReturn(storyListMock);

    // when
    Criteria criteria = Criteria.builder().title(randomAlphabetic(10)).authorName(randomAlphabetic(10)).build();
    Sort sort = Sort.builder().title(SortType.DESC).build();
    StorySearch storySearch = StorySearch.builder().criteria(criteria).sort(sort).build();
    List<StoryResponse> storyResponseList = storyService.searchStories(storySearch);

    // then
    verify(storyDaoMock).findAll(any(Specification.class));
    verify(storyService).applySorting(storyListMock, sort);
    assertThat(storyResponseList.size()).isEqualTo(3);
    assertThat(storyResponseList).isSortedAccordingTo(comparing(StoryResponse::getTitle).reversed());
  }

}
