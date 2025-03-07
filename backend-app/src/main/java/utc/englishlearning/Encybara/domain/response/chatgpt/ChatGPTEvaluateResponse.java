package utc.englishlearning.Encybara.domain.response.chatgpt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatGPTEvaluateResponse {
    private double score;
    private String evaluation;
    private String improvements;
}