package utc.englishlearning.Encybara.domain.response.notification;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResNotificationDTO {
    private long id;
    private String message;
    private boolean isRead;
    private Long userId;
    private Instant createdAt;

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
