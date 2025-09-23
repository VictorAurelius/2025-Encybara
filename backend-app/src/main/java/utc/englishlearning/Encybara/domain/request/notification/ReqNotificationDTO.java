package utc.englishlearning.Encybara.domain.request.notification;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.ImageNotiEnum;

@Getter
@Setter
public class ReqNotificationDTO {
    private String message;
    private Long userId;
    private Long entityId; // Thêm entityId
    private String entityType; // Thêm entityType
    private ImageNotiEnum img;
}
