package utc.englishlearning.Encybara.domain.response.notification;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResNotificationDTO {
    private Long id;
    private String message;
    private boolean isRead;
    private Long userId;
    private Instant createdAt;
    private Long entityId;
    private String entityType;
}
