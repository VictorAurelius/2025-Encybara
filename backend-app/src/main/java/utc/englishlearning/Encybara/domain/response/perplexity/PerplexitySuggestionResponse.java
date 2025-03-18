package utc.englishlearning.Encybara.domain.response.perplexity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerplexitySuggestionResponse {
    private String sampleAnswer; // Sample answer in English
}