package utc.englishlearning.Encybara.data.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
        // Bỏ qua các trường không khớp với entity
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Course> loadCourses() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "courses.json").getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<Course>>() {
            });
        }
    }

    public Map<String, Lesson> loadLessons() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "lessons.json").getInputStream()) {
            List<Map<String, Object>> lessonDataList = objectMapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Map<String, Lesson> lessonMap = new HashMap<>();

            for (Map<String, Object> data : lessonDataList) {
                String name = (String) data.get("name");
                Lesson lesson = new Lesson();
                lesson.setName(name);
                lesson.setSkillType(SkillTypeEnum.valueOf((String) data.get("skillType")));
                lesson.setSumQues((Integer) data.get("sumQues"));
                lesson.setCreateBy((String) data.get("createBy"));
                lessonMap.put(name, lesson);
            }

            return lessonMap;
        }
    }

    public Map<String, Question> loadQuestions() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "questions.json").getInputStream()) {
            List<Map<String, Object>> questionDataList = objectMapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Map<String, Question> questionMap = new HashMap<>();

            for (Map<String, Object> data : questionDataList) {
                String content = (String) data.get("quesContent");
                Question question = new Question();
                question.setQuesContent(content);
                question.setSkillType(SkillTypeEnum.valueOf((String) data.get("skillType")));
                question.setQuesType(QuestionTypeEnum.valueOf((String) data.get("quesType")));
                question.setPoint((Integer) data.get("point"));
                question.setCreateBy((String) data.get("createBy"));

                // Create choices
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) data.get("choices");
                for (Map<String, Object> choiceData : choices) {
                    Question_Choice choice = new Question_Choice();
                    choice.setChoiceContent((String) choiceData.get("content"));
                    choice.setChoiceKey((Boolean) choiceData.get("isCorrect"));
                    choice.setQuestion(question);
                }

                questionMap.put(content, question);
            }

            return questionMap;
        }
    }
}