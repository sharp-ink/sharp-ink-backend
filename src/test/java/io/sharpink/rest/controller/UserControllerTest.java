package io.sharpink.rest.controller;

import io.sharpink.SharpInkBackendApplication;
import io.sharpink.persistence.dao.story.StoryDao;
import io.sharpink.persistence.dao.user.UserDao;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.persistence.entity.user.User;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.rest.dto.response.user.UserResponse;
import io.sharpink.rest.exception.CustomApiError;
import io.sharpink.rest.exception.MissingEntity;
import io.sharpink.util.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SharpInkBackendApplication.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserDao userDao;
  @Autowired private StoryDao storyDao;

  User batman;
  User john;

  Story story1_Gotham_by_night;
  Story story2_Ode_to_my_Bat_family;
  Story story3_Superman_is_gay;

  @BeforeAll
  void init() {
    batman = User.builder().id(1L).nickname("Batman").build();
    john = User.builder().id(2L).nickname("John Doe").build();

    asList(batman, john).forEach(userDao::save);

    story1_Gotham_by_night = Story.builder()
      .id(1L)
      .author(batman)
      .title("Gotham by night")
      .lastModificationDate(LocalDateTime.now())
      .build();
    story2_Ode_to_my_Bat_family = Story.builder()
      .id(2L)
      .author(batman)
      .title("Ode to my Bat-family")
      .lastModificationDate(LocalDateTime.now().minusDays(1))
      .build();
    story3_Superman_is_gay = Story.builder()
      .id(3L)
      .author(batman)
      .title("Superman is gay")
      .lastModificationDate(LocalDateTime.now().minusDays(2))
      .build();

    asList(story1_Gotham_by_night, story2_Ode_to_my_Bat_family, story3_Superman_is_gay).forEach(storyDao::save);
  }

  @AfterAll
  void tearDown() {
    userDao.deleteAll();
  }

  @Test
  public void getUsers() throws Exception {
    // when
    String jsonResult = mockMvc.perform(get("/users"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    List<UserResponse> userResponses = JsonUtil.fromJsonArray(jsonResult, UserResponse.class);
    assertThat(userResponses.size()).isEqualTo(2);
    AssertableUser expectedUserBatman = buildAssertableUser(batman);
    AssertableUser expectedUserJohn = buildAssertableUser(john);
    AssertableUser user1 = buildAssertableUser(userResponses.get(0));
    AssertableUser user2 = buildAssertableUser(userResponses.get(1));
    assertThat(user1).isEqualTo(expectedUserBatman);
    assertThat(user2).isEqualTo(expectedUserJohn);

  }

  @Test
  public void getUser_given_existing_id_then_return_user() throws Exception {
    // when
    Long id = 1L;
    String jsonResult = mockMvc.perform(get("/users/" + id))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();


    // then
    AssertableUser user = JsonUtil.fromJson(jsonResult, AssertableUser.class);
    AssertableUser expectedUserBatman = buildAssertableUser(batman);
    assertThat(user).isEqualTo(expectedUserBatman);
  }

  @Test
  public void getUser_given_non_existing_id_then_return_error_response() throws Exception {
    // when
    Long id = 3L;
    String jsonResult = mockMvc.perform(get("/users/" + id))
      .andExpect(status().isNotFound())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    CustomApiError customApiError = JsonUtil.fromJson(jsonResult, CustomApiError.class);
    assertThat(customApiError.getCode()).isEqualTo(MissingEntity.USER.name());
    assertThat(customApiError.getMessage()).isEqualTo("User not found for id=" + id);
  }

  @Test
  public void getStories_given_existing_user_then_return_his_stories() throws Exception {
    // when
    Long id = 1L;
    String jsonResult = mockMvc.perform(get("/users/" + id + "/stories"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    List<StoryResponse> storyResponses = JsonUtil.fromJsonArray(jsonResult, StoryResponse.class);
    assertThat(storyResponses.size()).isEqualTo(3);
    AssertableStory expectedStory1_Gotham_by_night = buildAssertableStory(story1_Gotham_by_night);
    AssertableStory expectedStory2_Ode_to_my_Bat_family = buildAssertableStory(story2_Ode_to_my_Bat_family);
    AssertableStory expectedStory3_Superman_is_gay = buildAssertableStory(story3_Superman_is_gay);
    AssertableStory story1 = buildAssertableStory(storyResponses.get(0));
    AssertableStory story2 = buildAssertableStory(storyResponses.get(1));
    AssertableStory story3 = buildAssertableStory(storyResponses.get(2));
    assertThat(story1).isEqualTo(expectedStory1_Gotham_by_night);
    assertThat(story2).isEqualTo(expectedStory2_Ode_to_my_Bat_family);
    assertThat(story3).isEqualTo(expectedStory3_Superman_is_gay);
  }

  @Test
  public void getStories_given_existing_user_without_stories_then_return_empty_list() throws Exception {
    // when
    Long id = 2L;
    mockMvc.perform(get("/users/" + id + "/stories")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void getStories_given_non_existing_user_then_return_error_response() throws Exception {
    // when
    Long id = 42L;
    String jsonResult = mockMvc.perform(get("/users/" + id + "/stories"))
      .andExpect(status().isNotFound())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    CustomApiError customApiError = JsonUtil.fromJson(jsonResult, CustomApiError.class);
    assertThat(customApiError.getCode()).isEqualTo(MissingEntity.USER.name());
    assertThat(customApiError.getMessage()).isEqualTo("User not found for id=" + id);
  }

  // TODO tests des autres méthodes de UserController

  /*

  @Test
  public void updateUserProfile() {
    // given
    when(userServiceMock.updateUserProfile(anyLong(), any(UserPatchRequest.class))).thenReturn(new UserResponse());

    // when
    Long id = RandomUtils.nextLong();
    UserPatchRequest userPatchRequest = new UserPatchRequest();
    UserResponse userResponse = userController.updateUserProfile(id, userPatchRequest);

    // then
    ArgumentCaptor<Long> idAC = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<UserPatchRequest> userPatchRequestAC = ArgumentCaptor.forClass(UserPatchRequest.class);
    verify(userServiceMock).updateUserProfile(idAC.capture(), userPatchRequestAC.capture());
    assertEquals(id, idAC.getValue());
    assertTrue(userPatchRequest == userPatchRequestAC.getValue());
    assertThat(userResponse).isNotNull();
  }

  @Test
  public void getUserPreferences() {
    // given
    when(userServiceMock.getPreferences(anyLong())).thenReturn(new UserPreferencesDto());

    // when
    Long id = RandomUtils.nextLong();
    UserPreferencesDto userPreferencesDto = userController.getUserPreferences(id);

    // then
    ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
    verify(userServiceMock).getPreferences(idArgumentCaptor.capture());
    assertThat(idArgumentCaptor.getValue()).isEqualTo(id);
    assertThat(userPreferencesDto).isNotNull();
  }

  @Test
  public void updateUserPreferences() {
    // given
    when(userServiceMock.updateUserPreferences(anyLong(), any(UserPreferencesDto.class))).thenReturn(new UserPreferencesDto());

    // when
    Long id = RandomUtils.nextLong();
    UserPreferencesDto userPreferencesDto = new UserPreferencesDto();
    UserPreferencesDto updatedUserPreferencesDto = userController.updateUserPreferences(id, userPreferencesDto);

    // then
    ArgumentCaptor<Long> idAC = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<UserPreferencesDto> userPreferencesDtoAC = ArgumentCaptor.forClass(UserPreferencesDto.class);
    verify(userServiceMock).updateUserPreferences(idAC.capture(), userPreferencesDtoAC.capture());
    assertThat(idAC.getValue()).isEqualTo(id);
    assertTrue(userPreferencesDto == userPreferencesDtoAC.getValue());
    assertThat(updatedUserPreferencesDto).isNotNull();
  }
  */

  private AssertableUser buildAssertableUser(User user) {
    return AssertableUser.builder().id(user.getId()).nickname(user.getNickname()).build();
  }

  private AssertableUser buildAssertableUser(UserResponse userResponse) {
    return AssertableUser.builder().id(userResponse.getId()).nickname(userResponse.getNickname()).build();
  }

  private AssertableStory buildAssertableStory(Story story) {
    return AssertableStory.builder()
      .id(story.getId())
      .author(AssertableUser.builder().id(story.getAuthor().getId()).build())
      .title(story.getTitle())
      .build();
  }

  private AssertableStory buildAssertableStory(StoryResponse storyResponse) {
    return AssertableStory.builder()
      .id(storyResponse.getId())
      .author(AssertableUser.builder().id(storyResponse.getAuthorId()).build())
      .title(storyResponse.getTitle())
      .build();
  }

  @Getter
  @Builder
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  private static class AssertableUser {
    Long id;
    String nickname;
  }

  @Getter
  @Builder
  @EqualsAndHashCode
  @NoArgsConstructor
  @AllArgsConstructor
  private static class AssertableStory {
    Long id;
    AssertableUser author;
    String title;
  }
}
