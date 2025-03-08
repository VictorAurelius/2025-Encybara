package utc.englishlearning.Encybara.domain.response.answer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAnswerDTO {
    private Long id;
    private Long questionId;
    private String answerContent;
    private Integer pointAchieved;
    private Long sessionId;
    private String improvement; // Added improvement field
}