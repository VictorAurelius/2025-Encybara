package utc.englishlearning.Encybara.domain.request.perplexity;

import lombok.Data;

@Data
public class PerplexityRequest {
    private String question;
    private String userAnswer;
    private String prompt;
}