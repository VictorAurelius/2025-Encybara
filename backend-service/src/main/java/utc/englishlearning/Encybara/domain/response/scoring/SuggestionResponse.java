package utc.englishlearning.Encybara.domain.response.scoring;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuggestionResponse {
    private String keyPoints; // Key points to consider for answering
    private String sampleAnswer; // A sample answer structure
    private String tips; // Additional tips for answering
}