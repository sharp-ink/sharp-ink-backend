package io.sharpink.service;

import io.sharpink.mapper.story.StoryMapper;
import io.sharpink.persistence.dao.StoryDao;
import io.sharpink.persistence.entity.story.Story;
import io.sharpink.rest.dto.story.StoryDto;
import io.sharpink.rest.exception.UnprocessableEntity422Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static io.sharpink.rest.exception.UnprocessableEntity422ReasonEnum.TITLE_ALREADY_USED;

@Service
public class StoryService {

	private StoryDao storyDao;

	private StoryMapper storyMapper;

	@Autowired
	public StoryService(StoryDao storyDao, StoryMapper storyMapper) {
		this.storyDao = storyDao;
		this.storyMapper = storyMapper;
	}

	/**
	 * Récupère toutes les histoires présentes en base.
	 *
	 * @return Une {@code List<StoryDto>} représentant la liste des histoires, vide
	 *         s'il n'y aucune histoire.
	 */
	public List<StoryDto> getAllStories() {

		List<Story> stories = (List<Story>) storyDao.findAll();

		// on n'a pas besoin de charger les chapitres
		boolean shouldLoadChapters = false;

		return storyMapper.mapDtos(stories, shouldLoadChapters);

	}

	/**
	 * Récupère une histoire via son id.
	 *
	 * @param id L'id de l'histoire à récupérer.
	 * @return La {@code Story} correspondant à l'id passé en paramètre si elle
	 *         existe, null sinon.
	 */
	public Optional<StoryDto> getStory(Long id) {

		Optional<Story> storyOptional = storyDao.findById(id);

		if (storyOptional.isPresent()) {
			// on souhaite charger les chapitres
			boolean shouldLoadChapters = true;
			return Optional.of(storyMapper.mapDto(storyOptional.get(), shouldLoadChapters));
		} else {
			return Optional.empty();
		}

	}

	/**
	 * Crée et sauvegarde une histoire en base.
	 *
	 * @param storyDto Un objet contenant les informations de l'histoire à créer et
	 *                  sauvegarder.
	 * @return L'id de l'entité persistée, qui servira à identifier l'histoire de
	 *         manière unique.
	 */
	public Long createStory(StoryDto storyDto) {

	  if (storyWithSameTitleAlreadyExists(storyDto.getTitle())) {
	    throw new UnprocessableEntity422Exception(TITLE_ALREADY_USED);
    } else {
		Story story = storyDao.save(storyMapper.map(storyDto));
		// renvoie l'id généré lors de la persistance de l'entité
		return story.getId();
    }


	}

  private boolean storyWithSameTitleAlreadyExists(String title) {
	  Optional<Story> storyOptional = storyDao.findByTitle(title);
	  return storyOptional.isPresent();
	}

}
