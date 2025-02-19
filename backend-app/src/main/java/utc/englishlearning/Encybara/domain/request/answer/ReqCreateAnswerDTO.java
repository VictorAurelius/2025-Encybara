package utc.englishlearning.Encybara.domain.request.answer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateAnswerDTO {
    private Long questionId;
    private String[] answerContent; // Thay đổi từ String thành String[]
    private long sessionId;
}