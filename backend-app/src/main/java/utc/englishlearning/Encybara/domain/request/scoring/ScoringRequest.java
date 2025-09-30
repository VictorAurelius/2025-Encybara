package utc.englishlearning.Encybara.domain.request.scoring;

import lombok.Data;

@Data
public class ScoringRequest {
    private String question;
    private String userAnswer;
    private String prompt;
}