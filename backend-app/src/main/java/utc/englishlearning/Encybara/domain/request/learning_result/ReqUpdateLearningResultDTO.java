package utc.englishlearning.Encybara.domain.request.learning_result;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Getter
@Setter
public class ReqUpdateLearningResultDTO {
    @Min(1)
    @Max(7)
    private double listeningScore;

    @Min(1)
    @Max(7)
    private double speakingScore;

    @Min(1)
    @Max(7)
    private double readingScore;

    @Min(1)
    @Max(7)
    private double writingScore;
}