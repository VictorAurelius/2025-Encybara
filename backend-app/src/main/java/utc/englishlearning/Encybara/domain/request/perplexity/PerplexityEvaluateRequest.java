package utc.englishlearning.Encybara.domain.request.perplexity;

import lombok.Data;

@Data
public class PerplexityEvaluateRequest {
    private String userAnswer;
    private String question;
    private String prompt;
}