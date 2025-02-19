package utc.englishlearning.Encybara.domain.response.lesson;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResLessonResultDTO {
    private long id;
    private long lessonId;
    private long userId;
    private long sessionId;
    private long stuTime;
    private int point;
    private double comLevel;
    private long enrollmentId;
}