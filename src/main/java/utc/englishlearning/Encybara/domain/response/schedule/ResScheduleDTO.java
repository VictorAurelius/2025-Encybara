package utc.englishlearning.Encybara.domain.response.schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResScheduleDTO {
    private Long id;
    private Instant scheduleTime;
    private boolean isDaily;
    private Long userId;
    private Long courseId; // ID khóa học (nếu có)
}