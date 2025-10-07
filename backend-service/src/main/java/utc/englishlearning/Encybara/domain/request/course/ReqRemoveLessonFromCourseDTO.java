package utc.englishlearning.Encybara.domain.request.course;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRemoveLessonFromCourseDTO {
    private Long lessonId; // ID của lesson cần xóa
}