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
    private Instant errolDate;
    private boolean proStatus;
}
