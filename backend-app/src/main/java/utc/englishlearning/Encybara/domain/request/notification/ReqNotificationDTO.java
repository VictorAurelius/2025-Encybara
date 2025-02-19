package utc.englishlearning.Encybara.domain.request.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqNotificationDTO {
    private String message;
    private Long userId;
}
