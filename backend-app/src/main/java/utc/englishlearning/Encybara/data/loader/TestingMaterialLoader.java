package utc.englishlearning.Encybara.data.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.util.constant.*;

import java.io.IOException;
import java.io.InputStream;
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

    public void setDataPath(String courseGroup, String unitNumber, String paperNumber) {
        this.basePath = String.format("%s/%s/json/%s/%s/",
                JSON_BASE, courseGroup.toLowerCase(), unitNumber, paperNumber);
        System.out.println("Setting data path to: " + this.basePath);
    }

    public List<Course> loadCourses() throws IOException {
        System.out.println("Loading courses from: " + basePath + "courses.json");
        try (InputStream is = new ClassPathResource(basePath + "courses.json").getInputStream()) {
            List<Course> courses = objectMapper.readValue(is, new TypeReference<List<Course>>() {
            });
            System.out.println("Loaded " + courses.size() + " courses");
            return courses;
        }
    }

    public Map<String, Lesson> loadLessons() throws IOException {
        System.out.println("Loading lessons from: " + basePath + "lessons.json");
        try (InputStream is = new ClassPathResource(basePath + "lessons.json").getInputStream()) {
            List<Lesson> lessons = objectMapper.readValue(is, new TypeReference<List<Lesson>>() {
            });
            Map<String, Lesson> lessonMap = lessons.stream()
                    .collect(Collectors.toMap(Lesson::getName, lesson -> lesson));
            System.out.println("Loaded " + lessonMap.size() + " lessons");
            return lessonMap;
        }
    }

    public Map<String, Question> loadQuestions() throws IOException {
        Map<String, Question> questionMap = new HashMap<>();
        int fileCount = 1;
        int totalQuestions = 0;

        while (true) {
            String filename = String.format("question-%d.json", fileCount);
            String fullPath = basePath + filename;
            Resource resource = new ClassPathResource(fullPath);

            if (!resource.exists()) {
                if (fileCount == 1) {
                    System.out.println("WARNING: No question files found in path: " + basePath);
                }
                break;
            }

            try (InputStream is = resource.getInputStream()) {
                System.out.println("Loading questions from: " + filename);

                List<Map<String, Object>> questionDataList = objectMapper.readValue(is,
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                for (Map<String, Object> data : questionDataList) {
                    try {
                        String content = (String) data.get("quesContent");
                        Question question = createQuestionFromData(data);
                        questionMap.put(content, question);
                        totalQuestions++;
                    } catch (Exception e) {
                        System.err.println("Error processing question in " + filename + ": " + e.getMessage());
                    }
                }

                System.out.println("Loaded " + questionDataList.size() + " questions from " + filename);
                fileCount++;
            } catch (Exception e) {
                System.err.println("Error reading question file " + filename + ": " + e.getMessage());
                break;
            }
        }

        System.out.println("Total question files processed: " + (fileCount - 1));
        System.out.println("Total questions loaded: " + totalQuestions);
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

    public Map<String, List<Map<String, Object>>> loadMaterials() throws IOException {
        Map<String, List<Map<String, Object>>> materialMap = new HashMap<>();
        materialMap.put("lessons", new ArrayList<>());
        materialMap.put("questions", new ArrayList<>());

        String fullPath = basePath + "materials.json";
        Resource resource = new ClassPathResource(fullPath);

        if (!resource.exists()) {
            System.out.println("NOTE: No materials.json found in path: " + basePath);
            return materialMap;
        }

        try (InputStream is = resource.getInputStream()) {
            List<Map<String, Object>> materialDataList = objectMapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            // Separate materials by target type (lesson or question)
            for (Map<String, Object> material : materialDataList) {
                String lessonName = (String) material.get("lessonName");
                String questionContent = (String) material.get("questionContent");

                if (lessonName != null) {
                    materialMap.get("lessons").add(material);
                } else if (questionContent != null) {
                    materialMap.get("questions").add(material);
                }
            }

            System.out.println("Loaded " + materialMap.get("lessons").size() + " lesson materials");
            System.out.println("Loaded " + materialMap.get("questions").size() + " question materials");
        } catch (Exception e) {
            System.err.println("Warning: Error reading materials file: " + e.getMessage());
        }

        return materialMap;
    }
}
