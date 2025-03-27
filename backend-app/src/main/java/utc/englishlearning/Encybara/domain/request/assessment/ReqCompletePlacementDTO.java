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

    @NotNull(message = "Listening total points is required")
    @Min(value = 0, message = "Listening total points must be at least 0")
    private int listeningTotalPoints;

    @NotNull(message = "Speaking total points is required")
    @Min(value = 0, message = "Speaking total points must be at least 0")
    private int speakingTotalPoints;

    @NotNull(message = "Reading total points is required")
    @Min(value = 0, message = "Reading total points must be at least 0")
    private int readingTotalPoints;

    @NotNull(message = "Writing total points is required")
    @Min(value = 0, message = "Writing total points must be at least 0")
    private int writingTotalPoints;
}