package utc.englishlearning.Encybara.domain.request.course;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqAddLessonsToCourseDTO {
    private List<Long> lessonIds; // Danh sách ID của các lesson
}