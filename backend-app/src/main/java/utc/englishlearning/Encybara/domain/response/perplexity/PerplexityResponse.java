package utc.englishlearning.Encybara.domain.response.perplexity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerplexityResponse {
    private double score;
    private String evaluation;
    private String improvements;
}