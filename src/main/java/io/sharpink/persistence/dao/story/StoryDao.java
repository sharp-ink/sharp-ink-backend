package io.sharpink.persistence.dao.story;

import io.sharpink.persistence.entity.story.Story;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.upperCase;

@Repository
public interface StoryDao extends CrudRepository<Story, Long>, JpaSpecificationExecutor<Story> {

  @Query("SELECT s FROM Story s WHERE trim(lower(s.title)) = lower(:title)")
  Optional<Story> findByTitle(@Param(value = "title") String title);

  List<Story> findByAuthorId(Long authorId);

  static Specification<Story> hasTitleLike(String title) {
    return isEmpty(title) ?
      (story, cq, cb) -> cb.or() :
      (story, cq, cb) -> cb.like(cb.upper(story.get("title")), '%' + upperCase(title) + '%');
  }

  static Specification<Story> hasAuthorLike(String authorName) {
    return isEmpty(authorName) ?
      (story, cq, cb) -> cb.or() :
      (story, cq, cb) -> cb.like(cb.upper(story.get("author").get("nickname")), '%' + upperCase(authorName) + '%');
  }

}
