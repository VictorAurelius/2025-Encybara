package utc.englishlearning.Encybara.domain.request.assessment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCompletePlacementDTO {
    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotNull(message = "Completion level is required")
    @Min(value = 0, message = "Completion level must be at least 0")
    @Max(value = 100, message = "Completion level cannot exceed 100")
    private double comLevel;

    @NotNull(message = "Total points is required")
    @Min(value = 0, message = "Total points cannot be negative")
    private int totalPoints;

    @NotNull(message = "Listening score is required")
    @Min(value = 0, message = "Listening score must be at least 0")
    @Max(value = 10, message = "Listening score cannot exceed 10")
    private double listeningScore;

    @NotNull(message = "Speaking score is required")
    @Min(value = 0, message = "Speaking score must be at least 0")
    @Max(value = 10, message = "Speaking score cannot exceed 10")
    private double speakingScore;

    @NotNull(message = "Reading score is required")
    @Min(value = 0, message = "Reading score must be at least 0")
    @Max(value = 10, message = "Reading score cannot exceed 10")
    private double readingScore;

    @NotNull(message = "Writing score is required")
    @Min(value = 0, message = "Writing score must be at least 0")
    @Max(value = 10, message = "Writing score cannot exceed 10")
    private double writingScore;
}