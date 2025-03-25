package utc.englishlearning.Encybara.domain.response.enrollment;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;

import java.util.List;

@Getter
@Setter
public class ResEnrollmentWithRecommendationsDTO extends ResCalculateEnrollmentResultDTO {
    private List<CourseRecommendation> recommendations;
    private SkillProgress skillProgress;

    @Getter
    @Setter
    public static class CourseRecommendation {
        private Long courseId;
        private String courseName;
        private CourseTypeEnum courseType;
        private double diffLevel;
        private String reason;
    }

    @Getter
    @Setter
    public static class SkillProgress {
        private Double listeningProgress;
        private Double speakingProgress;
        private Double readingProgress;
        private Double writingProgress;
        private String focusArea; // Skill that needs most improvement
        private String strongestSkill;
    }
}