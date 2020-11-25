package io.sharpink.rest.controller;

import io.sharpink.rest.dto.request.story.ChapterRequest;
import io.sharpink.rest.dto.request.story.StoryPatchRequest;
import io.sharpink.rest.dto.request.story.StoryRequest;
import io.sharpink.rest.dto.request.story.search.StorySearch;
import io.sharpink.rest.dto.response.story.ChapterResponse;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.rest.exception.CustomApiError;
import io.sharpink.rest.exception.NotFound404Exception;
import io.sharpink.rest.exception.UnprocessableEntity422Exception;
import io.sharpink.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/stories")
public class StoryController {

  private StoryService storyService;

  @Autowired
  public StoryController(StoryService storyService) {
    this.storyService = storyService;
  }

  /**
   * Gets all {@code Story} with given publication status.
   */
  @GetMapping("")
  public List<StoryResponse> getStories(@RequestParam(required = false) Boolean published) {
    return storyService.getAllStories(published);
  }

  /**
   * Gets the {@code Story} with the given id.
   */
  @GetMapping("/{id}")
  public StoryResponse getStory(@PathVariable Long id) {
    Optional<StoryResponse> optionalStoryResponse = storyService.getStory(id);
    if (optionalStoryResponse.isPresent()) {
      return optionalStoryResponse.get();
    } else {
      throw new NotFound404Exception();
    }
  }

  /**
   * Crée la {@code Story} avec les informations fournies. Renvoie l'id généré
   * lors de l'insertion en base de données.
   */
  @PostMapping("")
  public ResponseEntity<?> createStory(@RequestBody StoryRequest storyRequest) {
    try {
      Long storyId = storyService.createStory(storyRequest);
      return new ResponseEntity<>(storyId, CREATED);
    } catch (UnprocessableEntity422Exception e) {
      return new ResponseEntity<>(new CustomApiError(e.getReason().name(), e.getMessage()), UNPROCESSABLE_ENTITY);
    }
  }

  @PostMapping("/search")
  public List<StoryResponse> search(StorySearch storySearch) {
    return storyService.searchStories(storySearch);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<?> updateStory(@PathVariable Long id, @RequestBody StoryPatchRequest storyPatchRequest) {
    try {
      StoryResponse storyResponse = storyService.updateStory(id, storyPatchRequest);
      return new ResponseEntity<>(storyResponse, OK);
    } catch (Exception e) {
      return new ResponseEntity<>(new CustomApiError(), INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> removeStory(@PathVariable Long id) {
    storyService.removeStory(id);
    return new ResponseEntity<>(NO_CONTENT);
  }

  /**
   * Creates a new chapter in the given story
   */
  @PostMapping("/{storyId}/chapters")
  public ResponseEntity<?> addChapter(@PathVariable Long storyId, @RequestBody ChapterRequest chapterRequest) {
    try {
      long chapterId = storyService.addChapter(storyId, chapterRequest);
      return new ResponseEntity<>(chapterId, CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(new CustomApiError(), INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Updates an existing chapter
   */
  @PutMapping("/{storyId}/chapters/{chapterPosition}")
  public ResponseEntity<?> updateChapter(@PathVariable Long storyId, @PathVariable Long chapterPosition, @RequestBody ChapterRequest chapterRequest) {
    try {
      ChapterResponse chapterResponse = storyService.updateChapter(storyId, chapterPosition.intValue(), chapterRequest);
      return new ResponseEntity<>(chapterResponse, OK);
    } catch (Exception e) {
      return new ResponseEntity<>(new CustomApiError(), INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/{storyId}/chapters/{chapterPosition}")
  public ResponseEntity<?> removeChapter(@PathVariable Long storyId, @PathVariable Long chapterPosition) {
    try {
      storyService.removeChapter(storyId, chapterPosition);
      return new ResponseEntity<>(NO_CONTENT);
    } catch (NotFound404Exception e) {
      return new ResponseEntity<>(new CustomApiError(e.getReason().name(), e.getMessage()), INTERNAL_SERVER_ERROR);
    }

  }
}
