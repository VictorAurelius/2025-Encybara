package utc.englishlearning.Encybara.domain.response.learning_result;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class ResLearningResultDTO {
    private long id;
    private double listeningScore;
    private double speakingScore;
    private double readingScore;
    private double writingScore;
    private Instant lastUpdated;

    // Previous scores for tracking progress
    private double previousListeningScore;
    private double previousSpeakingScore;
    private double previousReadingScore;
    private double previousWritingScore;

    // Progress calculations
    private double listeningProgress;
    private double speakingProgress;
    private double readingProgress;
    private double writingProgress;

    // Overall score (average of all skills)
    private double overallScore;
}