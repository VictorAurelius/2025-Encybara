package utc.englishlearning.Encybara.domain.response.course;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResCourseDTO {
    private long id;
    private String name;
    private String intro;
    private double diffLevel;
    private double recomLevel;
    private CourseTypeEnum courseType;
    private SpecialFieldEnum speciField;
    private String createBy;
    private Instant createAt;
    private String updateBy;
    private Instant updateAt;
    private Integer sumLesson;
    private List<Long> lessonIds;
    private CourseStatusEnum courseStatus;
    private String group;
}