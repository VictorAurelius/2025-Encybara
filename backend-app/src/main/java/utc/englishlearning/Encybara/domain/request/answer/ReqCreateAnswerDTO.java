package utc.englishlearning.Encybara.domain.request.answer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateAnswerDTO {
    private Long questionId;
    private String[] answerContent; // Array of answer content
    private long sessionId;
    private Integer pointAchieved; // Field for point achieved
    private String improvement; // New field for improvement suggestions
}