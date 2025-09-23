package utc.englishlearning.Encybara.domain.request.lesson;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqAddQuestionsToLessonDTO {
    private List<Long> questionIds;
}