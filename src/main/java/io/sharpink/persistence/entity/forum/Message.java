package io.sharpink.persistence.entity.forum;

import io.sharpink.persistence.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FORUM_MESSAGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message implements Comparable<Message> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @ManyToOne(optional = false)
  protected Thread thread;

  @ManyToOne(optional = false)
  @JoinColumn(name = "author_id")
  protected User author;

  @Column(name = "date", nullable = false)
  protected LocalDateTime publicationDate;

  @Column(name = "number", nullable = false, unique = true, updatable = false)
  protected Integer number;

  @Column(nullable = false, columnDefinition = "TEXT")
  protected String content;

  @Override
  public int compareTo(Message o) {
    return this.publicationDate.compareTo(o.publicationDate);
  }
}
