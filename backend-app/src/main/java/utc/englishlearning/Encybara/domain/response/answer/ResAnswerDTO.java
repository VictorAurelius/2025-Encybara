package utc.englishlearning.Encybara.domain.response.answer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAnswerDTO {
    private Long id;
    private Long questionId;
    private String answerContent;
    private int pointAchieved;
    private long sessionId;
}