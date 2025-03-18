package utc.englishlearning.Encybara.data.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.util.constant.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class JsonDataLoader {
    private final ObjectMapper objectMapper;
    private final String DATA_PATH = "data/ket1/";

    public JsonDataLoader() {
        this.objectMapper = new ObjectMapper();
    }

    public List<Course> loadCourses() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "courses.json").getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<Course>>() {
            });
        }
    }

    public Map<String, Lesson> loadLessons() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "lessons.json").getInputStream()) {
            List<LessonData> lessonDataList = objectMapper.readValue(is, new TypeReference<List<LessonData>>() {
            });
            Map<String, Lesson> lessonMap = new HashMap<>();

            for (LessonData data : lessonDataList) {
                Lesson lesson = new Lesson();
                lesson.setName(data.getName());
                lesson.setSkillType(SkillTypeEnum.valueOf(data.getSkillType()));
                lesson.setSumQues(data.getSumQues());
                lesson.setCreateBy(data.getCreateBy());
                lessonMap.put(data.getId(), lesson);
            }

            return lessonMap;
        }
    }

    public Map<String, Question> loadQuestions() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "questions.json").getInputStream()) {
            List<QuestionData> questionDataList = objectMapper.readValue(is, new TypeReference<List<QuestionData>>() {
            });
            Map<String, Question> questionMap = new HashMap<>();

            for (QuestionData data : questionDataList) {
                Question question = new Question();
                question.setQuesContent(data.getQuesContent());
                question.setSkillType(SkillTypeEnum.valueOf(data.getSkillType()));
                question.setQuesType(QuestionTypeEnum.valueOf(data.getQuesType()));
                question.setPoint(data.getPoint());
                question.setCreateBy(data.getCreateBy());
                questionMap.put(data.getId(), question);

                // Create choices for the question
                for (ChoiceData choiceData : data.getChoices()) {
                    Question_Choice choice = new Question_Choice();
                    choice.setChoiceContent(choiceData.getContent());
                    choice.setChoiceKey(choiceData.getIsCorrect());
                    choice.setQuestion(question);
                }
            }

            return questionMap;
        }
    }

    // Simple data classes for JSON mapping
    private static class LessonData {
        private String id;
        private String name;
        private String skillType;
        private Integer sumQues;
        private String createBy;
        private List<String> questionIds;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public List<String> getQuestionIds() {
            return questionIds;
        }

        public void setQuestionIds(List<String> questionIds) {
            this.questionIds = questionIds;
        }
    }

    private static class QuestionData {
        private String id;
        private String quesContent;
        private String skillType;
        private String quesType;
        private Integer point;
        private String createBy;
        private List<ChoiceData> choices;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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
    }

    private static class ChoiceData {
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
    }
}