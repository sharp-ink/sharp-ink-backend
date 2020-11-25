package io.sharpink.service;

import io.sharpink.UserMockUtil;
import io.sharpink.mapper.story.StoryMapper;
import io.sharpink.mapper.user.UserMapper;
import io.sharpink.persistence.dao.user.UserDao;
import io.sharpink.persistence.entity.story.ChaptersLoadingStrategy;
import io.sharpink.persistence.entity.story.StoriesLoadingStrategy;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.persistence.entity.user.User;
import io.sharpink.persistence.entity.user.UserDetails;
import io.sharpink.persistence.entity.user.UserPreferences;
import io.sharpink.persistence.entity.user.preferences.Theme;
import io.sharpink.rest.controller.StoryMockUtil;
import io.sharpink.rest.dto.request.user.UserPatchRequest;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.rest.dto.response.user.UserDetailsResponse;
import io.sharpink.rest.dto.response.user.UserResponse;
import io.sharpink.rest.dto.shared.user.preferences.AppearanceDto;
import io.sharpink.rest.dto.shared.user.preferences.UserPreferencesDto;
import io.sharpink.rest.exception.MissingEntity;
import io.sharpink.rest.exception.NotFound404Exception;
import io.sharpink.service.picture.PictureManagementService;
import io.sharpink.util.json.JsonUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.sharpink.constant.Constants.USERS_PROFILE_PICTURES_PATH;
import static io.sharpink.constant.Constants.USERS_PROFILE_PICTURES_WEB_URL;
import static java.util.Arrays.asList;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserDao userDaoMock;
  @Mock StoryMapper storyMapperMock;
  @Mock PictureManagementService pictureManagementServiceMock;
  @Spy UserMapper userMapper = new UserMapper(storyMapperMock);

  @InjectMocks
  UserService userService = new UserService(userDaoMock, userMapper, storyMapperMock, pictureManagementServiceMock);

  @Test
  void getAllUsers() {
    // given
    // List<User> with an random size of 0 to 4 items
    List<User> userListMock = IntStream.range(0, RandomUtils.nextInt(0, 5))
      .mapToObj(i -> new User())
      .collect(Collectors.toList());
    when(userDaoMock.findAll()).thenReturn(userListMock);
    int usersCount = userListMock.size();

    // when
    List<UserResponse> userResponseList = userService.getAllUsers();

    // then
    verify(userDaoMock).findAll();
    verify(userMapper).toUserResponseList(anyList(), eq(StoriesLoadingStrategy.DISABLED));
    assertThat(userResponseList).isNotNull();
    assertThat(userResponseList.size()).isEqualTo(usersCount);
  }

  @Test
  void getUser_UserExists() {
    // given
    User userMock = new User();
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));

    // when
    Long id = RandomUtils.nextLong();
    UserResponse userResponse = userService.getUser(id);

    // then
    verify(userDaoMock).findById(id);
    verify(userMapper).toUserResponse(any(User.class), eq(StoriesLoadingStrategy.DISABLED));
    assertThat(userResponse).isNotNull();
  }

  @Test
  void getUser_UserDoesNotExist() {
    // given
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.empty());

    // when
    Long id = RandomUtils.nextLong();
    assertThatThrownBy(() -> userService.getUser(id))
      .isInstanceOf(NotFound404Exception.class)
      .hasMessage("User not found for id=" + id)
      .extracting("reason").isEqualTo(MissingEntity.USER);

    // then
    verify(userDaoMock).findById(id);
    verify(userMapper, never()).toUserResponse(any(User.class), any(StoriesLoadingStrategy.class));
  }

  @Test
  void getStories_UserExists() {
    // given
    // A user with a random number of stories (up to 4 stories), each having a different last modification date
    User userMock = User.builder()
      .stories(IntStream.range(0, RandomUtils.nextInt(0, 5))
        .mapToObj(i -> {
          Story story = StoryMockUtil.getStoryMock();
          story.setTitle("Story " + i);
          story.setLastModificationDate(LocalDateTime.now().minusDays(RandomUtils.nextInt(1, 1000)));
          return story;
        })
        .collect(Collectors.toList()))
      .build();
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));
    when(storyMapperMock.toStoryResponseList(anyList(), any(ChaptersLoadingStrategy.class))).thenCallRealMethod();
    if (userMock.getStories().size() > 0) {
      when(storyMapperMock.toStoryResponse(any(Story.class), any(ChaptersLoadingStrategy.class))).thenCallRealMethod();
    }

    // when
    Long userId = RandomUtils.nextLong();
    List<StoryResponse> storyResponses = userService.getStories(userId);

    // then
    verify(userDaoMock).findById(userId);
    verify(storyMapperMock).toStoryResponseList(userMock.getStories()
      .stream()
      .sorted(reverseOrder())
      .collect(toList()), ChaptersLoadingStrategy.NONE);
    assertThat(storyResponses.size()).isEqualTo(userMock.getStories().size());
    assertThat(storyResponses)
      .isSortedAccordingTo(Comparator.comparing(StoryResponse::getLastModificationDate).reversed());
  }

  @Test
  void getStories_UserDoesNotExist() {
    // given
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.empty());

    // when
    Long userId = RandomUtils.nextLong();
    assertThatThrownBy(() -> userService.getStories(userId))
      .isInstanceOf(NotFound404Exception.class)
      .hasMessage("User not found for id=" + userId)
      .extracting("reason").isEqualTo(MissingEntity.USER);

    // then
    verify(userDaoMock).findById(userId);
    verify(storyMapperMock, never()).toStoryResponseList(anyList(), any(ChaptersLoadingStrategy.class));
  }

  @Test
  void updateUserProfile_UserDoesNotExist() {
    // given
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.empty());

    // when
    Long id = RandomUtils.nextLong();
    assertThatThrownBy(() -> userService.updateUserProfile(id, any(UserPatchRequest.class)))
      .isInstanceOf(NotFound404Exception.class)
      .hasMessage("User not found for id=" + id)
      .extracting("reason").isEqualTo(MissingEntity.USER);

    // then
    verify(userDaoMock).findById(id);
    verifyNoMoreInteractions(userDaoMock, userMapper);
  }

  @Test
  void updateUserProfile_UserExist_UpdateBasicInfoButNotProfilePicture() {
    // given
    UserPatchRequest userPatchRequest = UserPatchRequest.builder()
      .nickname("Mithrandir")
      .email("gandalf@middle-earth.com")
      .firstName("Gandalf")
      .lastName("The Grey")
      .profilePicture(null) // assume that we don't want to update the profile picture
      .build();

    // assume arbitrary existing profile picture (either empty or not)
    String profilePictureMock = RandomUtils.nextBoolean() == true ? null : "http://sharpink.io/sharpink/users/gandalf/gandalf.png";
    User userMock = User.builder()
      .userDetails(UserDetails.builder().profilePicture(profilePictureMock).build())
      .build();
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));

    // when
    Long id = RandomUtils.nextLong();
    UserResponse updatedUserResponse = userService.updateUserProfile(id, userPatchRequest);

    // then
    verify(userDaoMock).findById(id);
    verify(userMapper).toUserDetails(userPatchRequest);
    verify(userDaoMock).save(userMock);
    verify(userMapper).toUserResponse(userMock, StoriesLoadingStrategy.DISABLED);

    assertThat(updatedUserResponse.getNickname()).isEqualTo("Mithrandir");
    assertThat(updatedUserResponse.getEmail()).isEqualTo("gandalf@middle-earth.com");
    UserDetailsResponse userDetailsResponse = updatedUserResponse.getUserDetails();
    assertThat(userDetailsResponse.getFirstName()).isEqualTo("Gandalf");
    assertThat(userDetailsResponse.getLastName()).isEqualTo("The Grey");
    // the new profile picture should not have been updated
    assertThat(userDetailsResponse.getProfilePicture()).isEqualTo(profilePictureMock);

    verifyNoInteractions(pictureManagementServiceMock);
  }

  @Test
  void updateUserProfile_UserExist_UpdateProfilePicture() throws IOException {
    // given
    UserPatchRequest userPatchRequest = UserPatchRequest.builder()
      .profilePicture("data:image/jpg;base64,/9j/4AAQSkZJRgABAQETCETCETC")
      .build();

    User userMock = User.builder().nickname("Superman").build();
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));

    // when
    Long id = RandomUtils.nextLong();
    UserResponse updatedUserResponse = userService.updateUserProfile(id, userPatchRequest);

    // then
    verify(userDaoMock).findById(id);

    String expectedProfilePictureFSPath = USERS_PROFILE_PICTURES_PATH + "/Superman/Superman.jpg";
    verify(pictureManagementServiceMock).storePictureOnFileSystem("/9j/4AAQSkZJRgABAQETCETCETC", expectedProfilePictureFSPath);

    String expectedProfilePicture = USERS_PROFILE_PICTURES_WEB_URL + "/Superman/Superman.jpg";
    assertThat(updatedUserResponse.getUserDetails()).isNotNull();
    assertThat(updatedUserResponse.getUserDetails().getProfilePicture()).isEqualTo(expectedProfilePicture);
  }

  @Test
  void getPreferences_UserDoesNotExist() {
    // given
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.empty());

    // when
    Long id = RandomUtils.nextLong();
    assertThatThrownBy(() -> userService.getPreferences(id)).isInstanceOf(NotFound404Exception.class)
      .hasMessage("User not found for id=" + id)
      .extracting("reason")
      .isEqualTo(MissingEntity.USER);

    // then
    verify(userDaoMock).findById(id);
    verifyNoInteractions(userMapper);
  }

  @Test
  void getPreferences_UserExistsButHasNoPreferences() {
    // given
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(new User()));

    // when
    Long userId = RandomUtils.nextLong();
    UserPreferencesDto userPreferencesDto = userService.getPreferences(userId);

    // then
    verify(userDaoMock).findById(userId);
    assertThat(userPreferencesDto).isEqualTo(new UserPreferencesDto());
  }

  @Test
  void getPreferences_UserExistsAndHasSomePreferences() {
    // given
    User userMock = UserMockUtil.getUserMock();
    userMock.setUserPreferences(UserMockUtil.getUserPreferencesMock());
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));

    // when
    Long userId = RandomUtils.nextLong();
    UserPreferencesDto userPreferencesDto = userService.getPreferences(userId);

    // then
    verify(userDaoMock).findById(userId);
    verify(userMapper).toUserPreferencesDto(userMock.getUserPreferences().get());
    assertThat(userPreferencesDto).isNotNull();
    assertThat(userPreferencesDto.getAppearance()).isNotNull();
    assertThat(userPreferencesDto.getAppearance().getTheme()).isEqualTo(Theme.CANDY);
  }

  @Test
  void updateUserPreferences_UserDoesNotExist() {
    // given
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.empty());

    // when
    Long userId = RandomUtils.nextLong();
    assertThatThrownBy(() -> userService.updateUserPreferences(userId, new UserPreferencesDto()))
      .hasMessage("User not found for id=" + userId)
      .extracting("reason").isEqualTo(MissingEntity.USER);

    // then
    verify(userDaoMock).findById(userId);
    verifyNoMoreInteractions(userDaoMock, userMapper);
  }

  @Test
  void updateUserPreferences_UserHasNoPreferences() {
    // given
    User userMock = UserMockUtil.getUserMock();
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));

    // when
    Long userId = RandomUtils.nextLong();
    UserPreferencesDto userPreferencesDto = UserPreferencesDto.builder()
      .appearance(AppearanceDto.builder().theme(Theme.MARINE).build())
      .build();
    UserPreferencesDto updatedUserPreferencesDto = userService.updateUserPreferences(userId, userPreferencesDto);

    // then
    verify(userDaoMock).findById(userId);
    verify(userDaoMock).save(userMock);
    verify(userMapper).toUserPreferencesDto(any(UserPreferences.class));
    assertThat(updatedUserPreferencesDto).isNotNull();
    assertThat(updatedUserPreferencesDto.getAppearance()).isNotNull();
    assertThat(updatedUserPreferencesDto.getAppearance().getTheme()).isEqualTo(Theme.MARINE);
  }

  @Test
  void updateUserPreferences_UserHasAlreadySomePreferences() {
    // given
    User userMock = UserMockUtil.getUserMock();
    Theme originalPreferredTheme = pickRandomTheme();
    userMock.setUserPreferences(UserPreferences.builder()
      .user(userMock)
      .preferences(JsonUtil.toJson(UserPreferencesDto.builder()
        .appearance(AppearanceDto.builder().theme(originalPreferredTheme).build())
        .build()))
      .build());
    when(userDaoMock.findById(anyLong())).thenReturn(Optional.of(userMock));

    // when
    Long userId = RandomUtils.nextLong();
    Theme newTheme = pickRandomThemeDifferentFrom(originalPreferredTheme);
    UserPreferencesDto userPreferencesDto = UserPreferencesDto.builder()
      .appearance(AppearanceDto.builder().theme(newTheme).build())
      .build();
    UserPreferencesDto updatedUserPreferencesDto = userService.updateUserPreferences(userId, userPreferencesDto);

    // then
    verify(userDaoMock).findById(userId);
    verify(userDaoMock).save(userMock);
    verify(userMapper).toUserPreferencesDto(any(UserPreferences.class));
    assertThat(updatedUserPreferencesDto).isNotNull();
    assertThat(updatedUserPreferencesDto.getAppearance()).isNotNull();
    assertThat(updatedUserPreferencesDto.getAppearance()
      .getTheme()).as("Preferred theme should be changed from %s to %s", originalPreferredTheme, newTheme)
      .isEqualTo(newTheme);
  }

  private Theme pickRandomTheme() {
    List<Theme> themeValues = asList(Theme.values());
    Collections.shuffle(themeValues);
    return themeValues.stream().findFirst().get();
  }

  private Theme pickRandomThemeDifferentFrom(Theme currentTheme) {
    List<Theme> themeValues = asList(Theme.values());
    Collections.shuffle(themeValues);
    return themeValues.stream().filter(t -> t != currentTheme).findFirst().get();
  }
}
