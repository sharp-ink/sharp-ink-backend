package io.sharpink.persistence.dao.story;

import io.sharpink.persistence.entity.story.Story;
import io.sharpink.persistence.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static io.sharpink.persistence.dao.story.StoryDao.hasTitleLike;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StoryDaoTest {

  @Autowired StoryDao storyDao;
  @Autowired TestEntityManager entityManager;

  @BeforeEach
  void init() {
    User batman = User.builder().nickname("Batman").build();
    User aCoder = User.builder().nickname("John Doe").build();

    asList(batman, aCoder).forEach(entityManager::persist);

    //@formatter:off
    asList(
      Story.builder().title("Gotham City by night").author(batman).build(),
      Story.builder().title("The Dark Knight: savior of Gotham").author(batman).build(),
      Story.builder().title("Become the best developer in the universe in one month!").author(aCoder).build(),
      Story.builder().title("Did you really believe my first book's title ?").author(aCoder).build(),
      Story.builder().title("Autumn Boot").author(aCoder).build()
    ).forEach(entityManager::persist);
    entityManager.flush();
    //@formatter:on
  }

  // Tests testing only the 'hasTitleLike' Specification

  @Test
  @DisplayName("No story should be found when finding by title and passing a null value")
  void findAll_HasTitleLike_NullParameter() {
    // when
    List<Story> stories = storyDao.findAll(hasTitleLike(null));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("No story should be found when finding by title and passing an empty value")
  void findAll_HasTitleLike_EmptyParameter() {
    // when
    List<Story> stories = storyDao.findAll(hasTitleLike(""));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should return 2 stories when finding by title 'Gotham' with correct case")
  void findAll_HasTitleLike_MatchingStoriesExist() {
    // when
    List<Story> storiesWithTitleContaingBatman = storyDao.findAll(hasTitleLike("Gotham"));

    // then
    assertThatTwoStoriesFromUserBatmanHaveBeenFound(storiesWithTitleContaingBatman);
  }

  @Test
  @DisplayName("Should return 2 stories when finding by title 'gotham' (case should be ignored)")
  void findAll_HasTitleLike_MatchingStoriesExistLowerCase() {
    // when
    List<Story> storiesWithTitleContaingBatman = storyDao.findAll(hasTitleLike("gotham"));

    // then
    assertThatTwoStoriesFromUserBatmanHaveBeenFound(storiesWithTitleContaingBatman);
  }

  // Tests testing only the 'hasAuthorLike' Specification

  @Test
  @DisplayName("No story should be found when finding by author and passing a null value")
  void findAll_HasAuthorLike_NullParameter() {
    // when
    List<Story> stories = storyDao.findAll(StoryDao.hasAuthorLike(null));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("No story should be found when finding by author and passing an empty value")
  void findAll_HasAuthorLike_EmptyParameter() {
    // when
    List<Story> stories = storyDao.findAll(StoryDao.hasAuthorLike(""));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should return 3 stories when finding by author 'Doe' with correct case")
  void findAll_HasAuthorLike_MatchingStoriesExist() {
    // when
    List<Story> storiesWithAuthorJohnDoe = storyDao.findAll(StoryDao.hasAuthorLike("Doe"));

    // then
    assertThatThreeStoriesFromUserJohnDoeHaveBeenFound(storiesWithAuthorJohnDoe);
  }

  @Test
  @DisplayName("Should return 3 stories when finding by author 'jo' (case should be ignored)")
  void findAll_HasAuthorLike_MatchingStoriesExistLowerCase() {
    // when
    List<Story> storiesWithAuthorJohnDoe = storyDao.findAll(StoryDao.hasAuthorLike("jo"));

    // then
    assertThatThreeStoriesFromUserJohnDoeHaveBeenFound(storiesWithAuthorJohnDoe);
  }

  // Tests testing both 'hasTitleLike' and 'hasAuthorLike' together

  @Test
  @DisplayName("No story should be found when finding by not matching title and not matching author")
  void findAll_HasTitleLike_And_HasAuthorLike_NoMatchingValues() {
    // when
    List<Story> stories = storyDao.findAll(hasTitleLike("Why 42 is the answer").and(StoryDao.hasAuthorLike("God")));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("No story should be found when finding by existing title but not matching author")
  void findAll_HasTitleLike_And_HasAuthorLike_MatchingTitleButNotMatchingAuthor() {
    // when
    // the story 'Gotham City by night' is by Batman, not by Robin
    List<Story> stories = storyDao.findAll(hasTitleLike("Gotham City by night").and(StoryDao.hasAuthorLike("Robin")));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("No story should be found when finding by existing author but no matching title")
  void findAll_HasTitleLike_And_HasAuthorLike_MatchingAuthorButNoMatchingTitle() {
    // when
    // There is no story written by Batman with name like '%Metropolis%'
    List<Story> stories = storyDao.findAll(hasTitleLike("Metropolis").and(StoryDao.hasAuthorLike("Batman")));

    // then
    assertThat(stories.size()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should return 1 story when finding by author + title")
  void findAll_HasTitleLike_And_HasAuthorLike_MatchingAuthorAndMatchingTitle() {
    // when
    // There is no story written by Batman with name like '%Metropolis%'
    List<Story> stories = storyDao.findAll(hasTitleLike("savior").and(StoryDao.hasAuthorLike("Batman")));

    // then
    assertThat(stories.size()).isEqualTo(1);
    Story story = stories.get(0);
    assertThat(story.getAuthor().getNickname()).isEqualTo("Batman");
    assertThat(story.getTitle()).isEqualTo("The Dark Knight: savior of Gotham");
  }


  private void assertThatTwoStoriesFromUserBatmanHaveBeenFound(List<Story> stories) {
    assertThat(stories.size()).isEqualTo(2);
    Collections.sort(stories, Comparator.comparing(Story::getTitle));
    Story story1 = stories.get(0);
    Story story2 = stories.get(1);
    assertThat(story1.getTitle()).isEqualTo("Gotham City by night");
    assertThat(story1.getAuthor().getNickname()).isEqualTo("Batman");
    assertThat(story2.getTitle()).isEqualTo("The Dark Knight: savior of Gotham");
    assertThat(story2.getAuthor().getNickname()).isEqualTo("Batman");
  }

  private void assertThatThreeStoriesFromUserJohnDoeHaveBeenFound(List<Story> stories) {
    assertThat(stories.size()).isEqualTo(3);
    Collections.sort(stories, Comparator.comparing(Story::getTitle));
    Story story1 = stories.get(0);
    Story story2 = stories.get(1);
    Story story3 = stories.get(2);
    assertThat(story1.getTitle()).isEqualTo("Autumn Boot");
    assertThat(story1.getAuthor().getNickname()).isEqualTo("John Doe");
    assertThat(story2.getTitle()).isEqualTo("Become the best developer in the universe in one month!");
    assertThat(story2.getAuthor().getNickname()).isEqualTo("John Doe");
    assertThat(story3.getTitle()).isEqualTo("Did you really believe my first book's title ?");
    assertThat(story3.getAuthor().getNickname()).isEqualTo("John Doe");
  }
}
