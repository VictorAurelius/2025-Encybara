package utc.englishlearning.Encybara.data.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.util.constant.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TestingMaterialLoader {
    private final ObjectMapper objectMapper;
    private String basePath;
    private static final String JSON_BASE = "data";

    public TestingMaterialLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setDataPath(String courseGroup, String testNumber, String paperNumber) {
        this.basePath = String.format("%s/%s/json/test%s/paper%s/",
                JSON_BASE, courseGroup.toLowerCase(), testNumber, paperNumber);
    }

    public List<Course> loadCourses() throws IOException {
        try (InputStream is = new ClassPathResource(basePath + "courses.json").getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<Course>>() {
            });
        }
    }

    public Map<String, Lesson> loadLessons() throws IOException {
        try (InputStream is = new ClassPathResource(basePath + "lessons.json").getInputStream()) {
            List<Lesson> lessons = objectMapper.readValue(is, new TypeReference<List<Lesson>>() {
            });
            return lessons.stream().collect(Collectors.toMap(Lesson::getName, lesson -> lesson));
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Question> loadQuestions() throws IOException {
        Map<String, Question> questionMap = new HashMap<>();

        // Scan for all question files in the directory
        ClassPathResource baseResource = new ClassPathResource(basePath);
        Path baseDirPath = Paths.get(baseResource.getURI());

        // List all question-*.json files
        List<Path> questionFiles = Files.list(baseDirPath)
                .filter(path -> path.getFileName().toString().matches("questions-\\d+\\.json"))
                .collect(Collectors.toList());

        // Load questions from each file
        for (Path questionFile : questionFiles) {
            try (InputStream is = Files.newInputStream(questionFile)) {
                List<Map<String, Object>> questionDataList = objectMapper.readValue(is,
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                for (Map<String, Object> data : questionDataList) {
                    String content = (String) data.get("quesContent");
                    Question question = createQuestionFromData(data);
                    questionMap.put(content, question);
                }
            }
        }

        return questionMap;
    }

    @SuppressWarnings("unchecked")
    private Question createQuestionFromData(Map<String, Object> data) {
        Question question = new Question();
        question.setQuesContent((String) data.get("quesContent"));
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

        return question;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> loadMaterials() throws IOException {
        try (InputStream is = new ClassPathResource(basePath + "materials.json").getInputStream()) {
            List<Map<String, Object>> materialDataList = objectMapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            return materialDataList.stream()
                    .collect(Collectors.toMap(
                            data -> (String) data.get("lessonName"),
                            data -> data,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new));
        }
    }
}
