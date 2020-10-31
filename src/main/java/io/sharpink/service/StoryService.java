package io.sharpink.service;

import io.scaunois.common.util.date.DateUtil;
import io.sharpink.mapper.story.ChapterMapper;
import io.sharpink.mapper.story.StoryMapper;
import io.sharpink.persistence.dao.story.ChapterDao;
import io.sharpink.persistence.dao.story.StoryDao;
import io.sharpink.persistence.entity.story.Chapter;
import io.sharpink.persistence.entity.story.ChaptersLoadingStrategy;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.rest.dto.request.story.ChapterRequest;
import io.sharpink.rest.dto.request.story.StoryPatchRequest;
import io.sharpink.rest.dto.request.story.StoryRequest;
import io.sharpink.rest.dto.response.story.ChapterResponse;
import io.sharpink.rest.dto.response.story.StoryResponse;
import io.sharpink.rest.exception.InternalError500Exception;
import io.sharpink.rest.exception.NotFound404Exception;
import io.sharpink.rest.exception.UnprocessableEntity422Exception;
import io.sharpink.service.picture.PictureManagementService;
import io.sharpink.util.picture.PictureUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.sharpink.constant.Constants.USERS_PROFILE_PICTURES_PATH;
import static io.sharpink.constant.Constants.USERS_PROFILE_PICTURES_WEB_URL;
import static io.sharpink.rest.exception.MissingEntity.CHAPTER;
import static io.sharpink.rest.exception.UnprocessableEntity422ReasonEnum.TITLE_ALREADY_USED;
import static java.util.stream.Collectors.toList;

@Service
public class StoryService {

  private StoryDao storyDao;
  private ChapterDao chapterDao;
  private StoryMapper storyMapper;
  ChapterMapper chapterMapper;
  private PictureManagementService pictureManagementService;

  @Autowired
  public StoryService(StoryDao storyDao, ChapterDao chapterDao, StoryMapper storyMapper, ChapterMapper chapterMapper, PictureManagementService pictureManagementService) {
    this.storyDao = storyDao;
    this.chapterDao = chapterDao;
    this.storyMapper = storyMapper;
    this.chapterMapper = chapterMapper;
    this.pictureManagementService = pictureManagementService;
  }

  /**
   * Récupère toutes les histoires avec au moins un chapitre présentes en base.
   *
   * @return Une {@code List<StoryDto>} représentant la liste des histoires, vide
   * s'il n'y aucune histoire.
   */
  public List<StoryResponse> getAllStories(Boolean published) {

    List<Story> stories = ((List<Story>) storyDao.findAll())
      .stream()
      .filter(story -> story.getChaptersNumber() != 0) // keep stories having at least 1 chapter
      .filter(story -> story.isPublished() == published.booleanValue() || published == null) // keep only if given published status (or no published status specified)
      .collect(toList());

    return storyMapper.toStoryResponseList(stories, ChaptersLoadingStrategy.ONLY_FIRST); // load only the first chapter (useful for preview)
  }

  /**
   * Get stories for a specific {@code User}
   *
   * @return A {@code List<StoryDto>} containing all stories of the given {@code User}, empty list if this user has no stories.
   */
  public List<StoryResponse> getStories(Long userId) {
    List<Story> stories = storyDao.findByAuthorId(userId);

    return storyMapper.toStoryResponseList(stories, ChaptersLoadingStrategy.NONE); // chapters are not necessary
  }

  /**
   * Récupère une histoire via son id.
   *
   * @return La {@code Story} correspondant à l'id passé en paramètre si elle
   * existe, null sinon.
   */
  public Optional<StoryResponse> getStory(Long id) {

    Optional<Story> storyOptional = storyDao.findById(id);

    if (storyOptional.isPresent()) {
      return Optional.of(storyMapper.toStoryResponse(storyOptional.get(), ChaptersLoadingStrategy.ALL)); // all chapters are requested
    } else {
      return Optional.empty();
    }

  }

  /**
   * Crée et sauvegarde une histoire en base.
   *
   * @param storyRequest Un objet contenant les informations de l'histoire à créer et
   *                     sauvegarder.
   * @return L'id de l'entité persistée, qui servira à identifier l'histoire de
   * manière unique.
   */
  public Long createStory(StoryRequest storyRequest) {
    if (storyWithSameTitleAlreadyExists(storyRequest.getTitle())) {
      throw new UnprocessableEntity422Exception(TITLE_ALREADY_USED);
    } else {
      Story story = storyMapper.toStory(storyRequest);
      Date now = DateUtil.toDate(LocalDateTime.now());
      story.setCreationDate(now);
      story.setLastModificationDate(now);
      story = storyDao.save(story);
      return story.getId(); // returns id of newly created entity
    }
  }

  /**
   * Met à jour une histoire.
   *
   * @param id                L'id de l'histoire à mettre à jour.
   * @param storyPatchRequest Les nouvelles informations (partielles) à ajouter à l'histoire.
   */
  public StoryResponse updateStory(Long id, StoryPatchRequest storyPatchRequest) {
    Optional<Story> storyOptional = storyDao.findById(id);
    if (storyOptional.isPresent()) {
      Story story = storyOptional.get();

      if (StringUtils.isNotEmpty(storyPatchRequest.getTitle())) {
        story.setTitle(storyPatchRequest.getTitle());
      }

      story.setOriginalStory(storyPatchRequest.isOriginalStory()); // will not very likely change but it is allowed

      if (storyPatchRequest.getType() != null) {
        story.setType(storyPatchRequest.getType());
      }

      if (storyPatchRequest.getSummary() != null) {
        story.setSummary(storyPatchRequest.getSummary());
      }

      if (StringUtils.isNotEmpty(storyPatchRequest.getThumbnail())) {
        String formImageData = storyPatchRequest.getThumbnail();
        String extension = PictureUtil.extractExtension(formImageData);
        String storyThumbnailWebUrl = USERS_PROFILE_PICTURES_WEB_URL + '/' + story.getAuthor().getNickname() + "/stories/" + story.getId() + "/thumbnail." + extension;
        story.setThumbnail(storyThumbnailWebUrl);
        try {
          String storyThumbnailFSPath = USERS_PROFILE_PICTURES_PATH + '/' + story.getAuthor().getNickname() + "/stories/" + story.getId() + "/thumbnail." + extension;
          pictureManagementService.storePictureOnFileSystem(formImageData, storyThumbnailFSPath);
        } catch (IOException e) {
          e.printStackTrace(); // TODO: use a logger instead
          throw new InternalError500Exception(e);
        }
      }

      if (storyPatchRequest.getPublished() != null) {
        story.setPublished(storyPatchRequest.getPublished());
      }

      Story updatedStory = storyDao.save(story);
      return storyMapper.toStoryResponse(updatedStory, ChaptersLoadingStrategy.NONE);
    } else {
      throw new NotFound404Exception();
    }
  }

  public void removeStory(Long id) {
    storyDao.deleteById(id);
  }

  /**
   * Creates and persists a new chapter for a given story
   *
   * @param chapterRequest an object with the informations of the chapter to be created
   * @return the id of the newly created chapter (unique)
   */
  public Long addChapter(Long storyId, ChapterRequest chapterRequest) {
    Optional<Story> storyOptional = storyDao.findById(storyId);
    if (storyOptional.isPresent()) {
      Story story = storyOptional.get();

      Chapter newChapter = chapterMapper.toChapter(chapterRequest);
      newChapter.setPosition(story.getChaptersNumber() + 1);
      newChapter.setStory(story);
      chapterDao.save(newChapter);

      story.getChapters().add(newChapter);
      story.setChaptersNumber(story.getChaptersNumber() + 1);
      storyDao.save(story);

      return newChapter.getId();
    } else {
      throw new NotFound404Exception();
    }
  }

  /**
   * Updates an existing chapter
   */
  public ChapterResponse updateChapter(Long storyId, int chapterPosition, ChapterRequest chapterRequest) {
    Optional<Story> storyOptional = storyDao.findById(storyId);
    if (storyOptional.isPresent()) {
      Story story = storyOptional.get();

      try {
        Chapter chapter = story.getChapters().get(chapterPosition - 1);
        chapter.setTitle(chapterRequest.getTitle());
        chapter.setContent(chapterRequest.getContent());

        Chapter updatedChapter = chapterDao.save(chapter);
        return chapterMapper.toChapterResponse(updatedChapter);
      } catch (IndexOutOfBoundsException e) {
        throw new NotFound404Exception();
      }
    } else {
      throw new NotFound404Exception();
    }
  }

  /**
   * Removes a chapter from a given story
   */
  public void removeChapter(Long storyId, Long chapterPosition) {
    Story story = storyDao.findById(storyId)
      .orElseThrow(() -> new NotFound404Exception(CHAPTER));

    List<Chapter> chapters = story.getChapters();
    if (chapters.size() >= chapterPosition) {
      chapters.remove(chapterPosition.intValue() - 1);
      story.setChaptersNumber(story.getChapters().size());
      shiftPositionsDown(chapters, chapterPosition);
      storyDao.save(story);
    } else {
      throw new NotFound404Exception(CHAPTER);
    }
  }

  private boolean storyWithSameTitleAlreadyExists(String title) {
    Optional<Story> storyOptional = storyDao.findByTitle(title);
    return storyOptional.isPresent();
  }

  private void shiftPositionsDown(List<Chapter> chapters, Long chapterPosition) {
    chapters.stream()
      .filter(c -> c.getPosition() >= chapterPosition)
      .forEach(c -> c.setPosition(c.getPosition() - 1));
  }
}
