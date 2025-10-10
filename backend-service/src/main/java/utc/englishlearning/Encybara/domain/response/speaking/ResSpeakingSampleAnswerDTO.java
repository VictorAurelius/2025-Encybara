package utc.englishlearning.Encybara.domain.response.speaking;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResSpeakingSampleAnswerDTO {

    private Long id;
    private String answerContent;
    private String description;
    private Integer difficultyLevel;
    private Double estimatedScore;
    private Long questionId;
    private String questionContent; // Optional - include question content for reference
    private String audioLink; // Link to audio file

    // Audit fields
    private String createBy;
    private Instant createAt;
    private String updateBy;
    private Instant updateAt;

    // Helper method to get difficulty level as string
    public String getDifficultyLevelText() {
        if (difficultyLevel == null)
            return "Unknown";
        return switch (difficultyLevel) {
            case 1 -> "Basic";
            case 2 -> "Elementary";
            case 3 -> "Intermediate";
            case 4 -> "Advanced";
            case 5 -> "Expert";
            default -> "Unknown";
        };
    }

    // Helper method to get estimated score range
    public String getScoreRange() {
        if (estimatedScore == null)
            return "Not specified";
        if (estimatedScore >= 90)
            return "Excellent (90-100)";
        if (estimatedScore >= 80)
            return "Good (80-89)";
        if (estimatedScore >= 70)
            return "Average (70-79)";
        if (estimatedScore >= 60)
            return "Below Average (60-69)";
        return "Poor (0-59)";
    }
}