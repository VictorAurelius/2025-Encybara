package utc.englishlearning.Encybara.domain.request.course;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
public class ReqUpdateCourseDTO {
    private String name;
    private String intro;
    private int diffLevel;
    private int recomLevel;
    private CourseTypeEnum courseType;
    private SpecialFieldEnum speciField;
}