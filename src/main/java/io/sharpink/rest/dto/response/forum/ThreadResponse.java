package io.sharpink.rest.dto.response.forum;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadResponse {
  private Long id;
  private Long authorId;
  private String authorNickname;
  private String title;
  @JsonFormat(pattern = "yyyyMMdd HH:mm:ss")
  private LocalDateTime creationDate;
  private int messagesCount; // toujours renseigné
  private MessageResponse lastMessage; // toujours renseigné
  private List<MessageResponse> messages; // pas toujours renseigné
}
