package utc.englishlearning.Encybara.domain.request.chatgpt;

import lombok.Data;

@Data
public class ChatGPTEvaluateRequest {
    private String userAnswer;
    private String question;
    private String prompt;
}