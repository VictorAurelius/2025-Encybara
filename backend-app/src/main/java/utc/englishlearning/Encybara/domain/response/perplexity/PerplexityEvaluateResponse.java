package utc.englishlearning.Encybara.domain.response.perplexity;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PerplexityEvaluateResponse {
    private double score;
    private String evaluation;
    private String improvements;
    private String error;
}