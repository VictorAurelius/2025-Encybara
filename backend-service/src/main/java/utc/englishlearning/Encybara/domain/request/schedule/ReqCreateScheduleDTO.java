package utc.englishlearning.Encybara.domain.request.schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ReqCreateScheduleDTO {
    private Long userId;
    private Instant scheduleTime;
    private boolean isDaily; // Để phân biệt lịch học hàng ngày
    private Long courseId; // ID khóa học (nếu có)
}