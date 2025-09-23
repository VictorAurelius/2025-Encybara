package utc.englishlearning.Encybara.domain.request.lesson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateLessonResultDTO {
    private long stuTime;
    private Long lessonId;
    private long sessionId;
    private long userId;
    private long enrollmentId;
}