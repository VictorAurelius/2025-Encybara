package utc.englishlearning.Encybara.domain.response.enrollment;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ResEnrollmentDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private Instant enrollDate;
    private boolean proStatus;
    private int totalPoints; // Total points for the course
    private double comLevel; // Course completion level
}
