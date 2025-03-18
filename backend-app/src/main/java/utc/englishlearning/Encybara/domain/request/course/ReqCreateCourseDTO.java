package utc.englishlearning.Encybara.domain.request.course;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
public class ReqCreateCourseDTO {
    private String name;
    private String intro;
    private double diffLevel;
    private double recomLevel;
    private CourseTypeEnum courseType;
    private SpecialFieldEnum speciField;
    private String group;
}