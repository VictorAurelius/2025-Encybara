package utc.englishlearning.Encybara.domain.response.enrollment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCalculateEnrollmentResultDTO {
    private Long enrollmentId; // ID của Enrollment
    private int totalPoints; // Điểm tổng
    private double comLevel; // Mức độ hoàn thành
}