package io.sharpink.rest.controller;

import io.sharpink.rest.dto.request.user.UserPatchRequest;
import io.sharpink.rest.dto.response.user.UserResponse;
import io.sharpink.rest.dto.shared.user.preferences.UserPreferencesDto;
import io.sharpink.rest.exception.CustomApiError;
import io.sharpink.rest.exception.NotFound404Exception;
import io.sharpink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

	private UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("")
	public List<UserResponse> getUsers() {
		return userService.getAllUsers();
	}

  @GetMapping("/{id}")
  public ResponseEntity<?> getUser(@PathVariable Long id) {
    try {
      UserResponse user = userService.getUser(id);
      return new ResponseEntity<>(user, HttpStatus.OK);
    } catch (NotFound404Exception e) {
      return new ResponseEntity<>(new CustomApiError(e.getReason().name(), e.getMessage()), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/{id}/stories")
  public ResponseEntity<?> getStories(@PathVariable Long id) {
    try {
      return new ResponseEntity<>(userService.getStories(id), HttpStatus.OK);
    } catch (NotFound404Exception e) {
      return new ResponseEntity<>(new CustomApiError(e.getReason().name(), e.getMessage()), HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping("/{id}/profile")
  public UserResponse updateUserProfile(@PathVariable Long id, @RequestBody UserPatchRequest userPatchRequest) {
    return userService.updateUserProfile(id, userPatchRequest);
  }

  @GetMapping("/{id}/preferences")
  public UserPreferencesDto getUserPreferences(@PathVariable Long id) {
    return userService.getPreferences(id);
  }

  @PatchMapping("/{id}/preferences")
  public UserPreferencesDto updateUserPreferences(@PathVariable Long id, @RequestBody UserPreferencesDto userPreferencesDto) {
    return userService.updateUserPreferences(id, userPreferencesDto);
  }
}
