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
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class JsonDataLoader {
    private final ObjectMapper objectMapper;
    private final String DATA_PATH = "data/ket1/";

    public JsonDataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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

    @SuppressWarnings("unchecked")
    public Map<String, Question> loadQuestions() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_PATH + "questions.json").getInputStream()) {
            List<Map<String, Object>> questionDataList = objectMapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Map<String, Question> questionMap = new HashMap<>();

            for (Map<String, Object> data : questionDataList) {
                String content = (String) data.get("quesContent");

                // Create question
                Question question = new Question();
                question.setQuesContent(content);
                question.setSkillType(SkillTypeEnum.valueOf((String) data.get("skillType")));
                question.setQuesType(QuestionTypeEnum.valueOf((String) data.get("quesType")));
                question.setPoint((Integer) data.get("point"));
                question.setCreateBy((String) data.get("createBy"));

                // Initialize collections
                question.setAnswers(new ArrayList<>());
                question.setLessonQuestions(new ArrayList<>());
                question.setQuestionChoices(new ArrayList<>());

                // Create choices for question
                List<Map<String, Object>> choicesData = (List<Map<String, Object>>) data.get("choices");
                if (choicesData != null) {
                    List<Question_Choice> choices = new ArrayList<>();
                    for (Map<String, Object> choiceData : choicesData) {
                        Question_Choice choice = new Question_Choice();
                        choice.setChoiceContent((String) choiceData.get("content"));
                        choice.setChoiceKey((Boolean) choiceData.get("isCorrect"));
                        choice.setQuestion(question);
                        choices.add(choice);
                    }
                    question.setQuestionChoices(choices);
                }

                questionMap.put(content, question);
            }

            return questionMap;
        }
    }
}