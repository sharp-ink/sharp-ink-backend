package io.sharpink.persistence.entity.forum;

import io.sharpink.persistence.entity.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FORUM_MESSAGE")
@Data
@NoArgsConstructor
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @ManyToOne(optional = false)
  protected Thread thread;

  @ManyToOne
  @JoinColumn(name = "author_id")
  protected User author;

  @Column(name="date", nullable = false)
  protected LocalDateTime publicationDate;

  @Column(nullable = false, columnDefinition = "TEXT")
  protected String content;

}