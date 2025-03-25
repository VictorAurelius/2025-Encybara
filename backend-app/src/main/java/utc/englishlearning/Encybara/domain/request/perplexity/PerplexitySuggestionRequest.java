package utc.englishlearning.Encybara.domain.request.perplexity;

import lombok.Data;

@Data
public class PerplexitySuggestionRequest {
    private String question;
    private String prompt; // Optional context/prompt for better suggestions
}