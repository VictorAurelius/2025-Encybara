package utc.englishlearning.Encybara.domain.request.lesson;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

@Getter
@Setter
public class ReqUpdateLessonDTO {
    private String name;
    private SkillTypeEnum skillType;
}