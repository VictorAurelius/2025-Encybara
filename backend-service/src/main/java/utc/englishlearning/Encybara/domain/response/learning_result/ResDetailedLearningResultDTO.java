package utc.englishlearning.Encybara.domain.response.learning_result;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResDetailedLearningResultDTO extends ResLearningResultDTO {
    private List<EnrollmentHistoryItem> enrollmentHistory;

    @Getter
    @Setter
    public static class EnrollmentHistoryItem {
        private long enrollmentId;
        private long courseId;
        private String courseName;
        private double diffLevel;
        private double comLevel;
        private double skillScore;
        private Instant enrollDate;
    }
}