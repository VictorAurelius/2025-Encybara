package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;
import utc.englishlearning.Encybara.data.loader.JsonDataLoader;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.service.FileStorageService;
import utc.englishlearning.Encybara.service.LearningMaterialService;
import utc.englishlearning.Encybara.util.ResourceMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KET1CourseDataSeeder {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final LessonQuestionRepository lessonQuestionRepository;
    private final JsonDataLoader jsonDataLoader;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;
    private final LearningMaterialService learningMaterialService;
    private final LearningMaterialRepository learningMaterialRepository;

    public KET1CourseDataSeeder(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            CourseLessonRepository courseLessonRepository,
            QuestionChoiceRepository questionChoiceRepository,
            LessonQuestionRepository lessonQuestionRepository,
            JsonDataLoader jsonDataLoader,
            ObjectMapper objectMapper,
            FileStorageService fileStorageService,
            LearningMaterialService learningMaterialService,
            LearningMaterialRepository learningMaterialRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.courseLessonRepository = courseLessonRepository;
        this.questionChoiceRepository = questionChoiceRepository;
        this.lessonQuestionRepository = lessonQuestionRepository;
        this.jsonDataLoader = jsonDataLoader;
        this.objectMapper = objectMapper;
        this.fileStorageService = fileStorageService;
        this.learningMaterialService = learningMaterialService;
        this.learningMaterialRepository = learningMaterialRepository;
    }

    @SuppressWarnings("unchecked")
    public void seedCourse() {
        try {
            // Load all data first
            List<Course> courses = jsonDataLoader.loadCourses();
            Map<String, Lesson> lessonsByName = jsonDataLoader.loadLessons();
            Map<String, Question> questionsByContent = jsonDataLoader.loadQuestions();
            Map<String, Map<String, Object>> materialsByLesson = jsonDataLoader.loadMaterials();

            for (Course course : courses) {
                // Check if course already exists
                Course existingCourse = courseRepository.findByName(course.getName());
                if (existingCourse != null) {
                    System.out.println(">>> SKIP: " + course.getName() + " already exists");
                    continue;
                }

                System.out.println(">>> START SEEDING: " + course.getName());

                // Save course
                course.setCreateAt(Instant.now());
                course = courseRepository.save(course);

                // Process each lesson using the lessonNames from Course entity
                Map<String, Object> courseData = objectMapper.convertValue(course, Map.class);
                List<String> lessonNames = (List<String>) courseData.get("lessonNames");

                if (lessonNames != null) {
                    course.setLessonNames(lessonNames);

                    for (String lessonName : lessonNames) {
                        Lesson lesson = lessonsByName.get(lessonName);
                        if (lesson == null) {
                            System.out.println(">>> WARNING: Lesson not found: " + lessonName);
                            continue;
                        }

                        // Save lesson
                        lesson.setCreateAt(Instant.now());
                        lesson = lessonRepository.save(lesson);

                        // Create course-lesson relationship
                        Course_Lesson courseLesson = new Course_Lesson();
                        courseLesson.setCourse(course);
                        courseLesson.setLesson(lesson);
                        courseLessonRepository.save(courseLesson);

                        // Process questions from lesson's questionContents
                        List<String> questionContents = lesson.getQuestionContents();
                        if (questionContents != null && !questionContents.isEmpty()) {
                            // Process each question for this lesson
                            for (String quesContent : questionContents) {
                                Question question = questionsByContent.get(quesContent);
                                if (question == null) {
                                    System.out.println(">>> WARNING: Question not found: " + quesContent);
                                    continue;
                                }

                                // Save question and its choices
                                for (Question_Choice choice : question.getQuestionChoices()) {
                                    choice.setQuestion(question);
                                }
                                question = questionRepository.save(question);

                                // Create lesson-question relationship
                                Lesson_Question lessonQuestion = new Lesson_Question();
                                lessonQuestion.setLesson(lesson);
                                lessonQuestion.setQuestion(question);
                                lessonQuestionRepository.save(lessonQuestion);

                                // Save question choices
                                for (Question_Choice choice : question.getQuestionChoices()) {
                                    questionChoiceRepository.save(choice);
                                }
                            }
                        }

                        // Process materials for this lesson
                        Map<String, Object> materialData = materialsByLesson.get(lessonName);
                        if (materialData != null) {
                            seedLearningMaterial(materialData, lesson);
                        }
                    }
                }

                System.out.println(">>> END SEEDING: " + course.getName());
            }
        } catch (IOException e) {
            System.err.println("Error seeding KET1 course data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedLearningMaterial(Map<String, Object> materialData, Lesson lesson) {
        try {
            String sourceFilePath = (String) materialData.get("materPath");
            String materType = (String) materialData.get("materType");
            String name = (String) materialData.get("name");

            // Load the image from resources
            try (InputStream is = new ClassPathResource(sourceFilePath).getInputStream()) {
                // Create temp file with original name
                String fileName = Path.of(sourceFilePath).getFileName().toString();
                Path tempFile = Files.createTempFile("temp_", fileName);
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

                // Create MultipartFile from the temp file
                MultipartFile multipartFile = new ResourceMultipartFile(
                        name,
                        fileName,
                        materType,
                        Files.readAllBytes(tempFile));

                // Store file using FileStorageService
                String materLink = learningMaterialService.store(multipartFile, "lessons/test1");

                // Create Learning_Material record
                Learning_Material material = new Learning_Material();
                material.setMaterLink(materLink);
                material.setMaterType(materType);
                material.setLesson(lesson);
                material.setUploadedAt(Instant.now());
                learningMaterialRepository.save(material);

                // Cleanup temp file
                Files.deleteIfExists(tempFile);

                System.out.println(">>> SEEDED MATERIAL: " + materLink + " for lesson: " + lesson.getName());
            }
        } catch (IOException e) {
            System.err.println("Error seeding learning material: " + e.getMessage());
            e.printStackTrace();
        }
    }
}