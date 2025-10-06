package utc.englishlearning.Encybara.domain.request.scoring;

import lombok.Data;

@Data
public class SuggestionRequest {
    private String question;
    private String prompt; // Optional context/prompt for better suggestions
}