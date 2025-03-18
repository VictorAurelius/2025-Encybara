package utc.englishlearning.Encybara.data.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.util.constant.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class JsonDataLoader {
    private final ObjectMapper objectMapper;

    public JsonDataLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public CourseData loadCourseData(String jsonPath) throws IOException {
        try (InputStream is = new ClassPathResource(jsonPath).getInputStream()) {
            return objectMapper.readValue(is, CourseData.class);
        }
    }

    // Data classes to map JSON structure
    public static class CourseData {
        private String name;
        private String intro;
        private Double diffLevel;
        private Double recomLevel;
        private String courseType;
        private Integer numLike;
        private String courseStatus;
        private Integer sumLesson;
        private String group;
        private String specialField;
        private String createBy;
        private List<LessonData> lessons;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIntro() {
            return intro;
        }

        public void setIntro(String intro) {
            this.intro = intro;
        }

        public Double getDiffLevel() {
            return diffLevel;
        }

        public void setDiffLevel(Double diffLevel) {
            this.diffLevel = diffLevel;
        }

        public Double getRecomLevel() {
            return recomLevel;
        }

        public void setRecomLevel(Double recomLevel) {
            this.recomLevel = recomLevel;
        }

        public String getCourseType() {
            return courseType;
        }

        public void setCourseType(String courseType) {
            this.courseType = courseType;
        }

        public Integer getNumLike() {
            return numLike;
        }

        public void setNumLike(Integer numLike) {
            this.numLike = numLike;
        }

        public String getCourseStatus() {
            return courseStatus;
        }

        public void setCourseStatus(String courseStatus) {
            this.courseStatus = courseStatus;
        }

        public Integer getSumLesson() {
            return sumLesson;
        }

        public void setSumLesson(Integer sumLesson) {
            this.sumLesson = sumLesson;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getSpecialField() {
            return specialField;
        }

        public void setSpecialField(String specialField) {
            this.specialField = specialField;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public List<LessonData> getLessons() {
            return lessons;
        }

        public void setLessons(List<LessonData> lessons) {
            this.lessons = lessons;
        }

        // Convert to domain object
        public Course toCourse() {
            Course course = new Course();
            course.setName(name);
            course.setIntro(intro);
            course.setDiffLevel(diffLevel);
            course.setRecomLevel(recomLevel);
            course.setCourseType(CourseTypeEnum.valueOf(courseType));
            course.setNumLike(numLike);
            course.setCourseStatus(CourseStatusEnum.valueOf(courseStatus));
            course.setSumLesson(sumLesson);
            course.setGroup(group);
            course.setSpeciField(SpecialFieldEnum.valueOf(specialField));
            course.setCreateBy(createBy);
            return course;
        }
    }

    public static class LessonData {
        private String name;
        private String skillType;
        private Integer sumQues;
        private String createBy;
        private List<QuestionData> questions;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSkillType() {
            return skillType;
        }

        public void setSkillType(String skillType) {
            this.skillType = skillType;
        }

        public Integer getSumQues() {
            return sumQues;
        }

        public void setSumQues(Integer sumQues) {
            this.sumQues = sumQues;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public List<QuestionData> getQuestions() {
            return questions;
        }

        public void setQuestions(List<QuestionData> questions) {
            this.questions = questions;
        }

        // Convert to domain object
        public Lesson toLesson() {
            Lesson lesson = new Lesson();
            lesson.setName(name);
            lesson.setSkillType(SkillTypeEnum.valueOf(skillType));
            lesson.setSumQues(sumQues);
            lesson.setCreateBy(createBy);
            return lesson;
        }
    }

    public static class QuestionData {
        private String quesContent;
        private String skillType;
        private String quesType;
        private Integer point;
        private String createBy;
        private List<ChoiceData> choices;

        // Getters and setters
        public String getQuesContent() {
            return quesContent;
        }

        public void setQuesContent(String quesContent) {
            this.quesContent = quesContent;
        }

        public String getSkillType() {
            return skillType;
        }

        public void setSkillType(String skillType) {
            this.skillType = skillType;
        }

        public String getQuesType() {
            return quesType;
        }

        public void setQuesType(String quesType) {
            this.quesType = quesType;
        }

        public Integer getPoint() {
            return point;
        }

        public void setPoint(Integer point) {
            this.point = point;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public List<ChoiceData> getChoices() {
            return choices;
        }

        public void setChoices(List<ChoiceData> choices) {
            this.choices = choices;
        }

        // Convert to domain object
        public Question toQuestion() {
            Question question = new Question();
            question.setQuesContent(quesContent);
            question.setSkillType(SkillTypeEnum.valueOf(skillType));
            question.setQuesType(QuestionTypeEnum.valueOf(quesType));
            question.setPoint(point);
            question.setCreateBy(createBy);
            return question;
        }
    }

    public static class ChoiceData {
        private String content;
        private Boolean isCorrect;

        // Getters and setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }

        // Convert to domain object
        public Question_Choice toChoice() {
            Question_Choice choice = new Question_Choice();
            choice.setChoiceContent(content);
            choice.setChoiceKey(isCorrect);
            return choice;
        }
    }
}