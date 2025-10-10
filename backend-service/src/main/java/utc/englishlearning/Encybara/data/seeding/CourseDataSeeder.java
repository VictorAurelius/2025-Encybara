package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;
import utc.englishlearning.Encybara.data.loader.TestingMaterialLoader;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;
import utc.englishlearning.Encybara.service.LearningMaterialService;
import utc.englishlearning.Encybara.util.ResourceMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CourseDataSeeder {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final LessonQuestionRepository lessonQuestionRepository;
    private final TestingMaterialLoader materialLoader;
    private final ObjectMapper objectMapper;
    private final LearningMaterialService learningMaterialService;
    private final LearningMaterialRepository learningMaterialRepository;
    private final SpeakingSampleAnswerRepository speakingSampleAnswerRepository;

    public CourseDataSeeder(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            CourseLessonRepository courseLessonRepository,
            QuestionChoiceRepository questionChoiceRepository,
            LessonQuestionRepository lessonQuestionRepository,
            TestingMaterialLoader materialLoader,
            ObjectMapper objectMapper,
            LearningMaterialService learningMaterialService,
            LearningMaterialRepository learningMaterialRepository,
            SpeakingSampleAnswerRepository speakingSampleAnswerRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.courseLessonRepository = courseLessonRepository;
        this.questionChoiceRepository = questionChoiceRepository;
        this.lessonQuestionRepository = lessonQuestionRepository;
        this.materialLoader = materialLoader;
        this.objectMapper = objectMapper;
        this.learningMaterialService = learningMaterialService;
        this.learningMaterialRepository = learningMaterialRepository;
        this.speakingSampleAnswerRepository = speakingSampleAnswerRepository;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void seedCourseData(String courseGroup, String unitNumber, String paperNumber) {
        try {
            // Set the data path for this specific course/test/paper
            materialLoader.setDataPath(courseGroup, unitNumber, paperNumber);

            // Load all data first
            List<Course> courses = materialLoader.loadCourses();
            Map<String, Lesson> lessonsByName = materialLoader.loadLessons();
            Map<String, Question> questionsByContent = materialLoader.loadQuestions();
            Map<String, List<Map<String, Object>>> materialsByTarget = materialLoader.loadMaterials();

            for (Course course : courses) {
                // Check if course already exists
                Course existingCourse = courseRepository.findByName(course.getName());
                if (existingCourse != null) {
                    System.out.println(">>> SKIP: " + course.getName() + " already exists");
                    continue;
                }

                System.out.println(">>> START SEEDING: " + course.getName());

                // Save course first
                course.setCreateAt(Instant.now());
                Course savedCourse = courseRepository.save(course);

                // Process course materials
                List<Map<String, Object>> courseMaterials = materialsByTarget.get("courses");
                if (courseMaterials != null) {
                    for (Map<String, Object> materialData : courseMaterials) {
                        if (savedCourse.getName().equals(materialData.get("courseName"))) {
                            seedLearningMaterial(materialData, null, null, savedCourse, courseGroup, unitNumber,
                                    paperNumber);
                        }
                    }
                }

                // Process each lesson using the lessonNames from Course entity
                Map<String, Object> courseData = objectMapper.convertValue(savedCourse, Map.class);
                List<String> lessonNames = (List<String>) courseData.get("lessonNames");

                if (lessonNames != null) {
                    savedCourse.setLessonNames(lessonNames);

                    for (String lessonName : lessonNames) {
                        Lesson lesson = lessonsByName.get(lessonName);
                        if (lesson == null) {
                            System.out.println(">>> WARNING: Lesson not found: " + lessonName);
                            continue;
                        }

                        // Save lesson
                        lesson.setCreateAt(Instant.now());
                        Lesson savedLesson = lessonRepository.save(lesson);

                        // Create course-lesson relationship
                        Course_Lesson courseLesson = new Course_Lesson();
                        courseLesson.setCourse(savedCourse);
                        courseLesson.setLesson(savedLesson);
                        courseLessonRepository.save(courseLesson);

                        // Process questions from lesson's questionContents
                        List<String> questionContents = lesson.getQuestionContents();
                        if (questionContents != null && !questionContents.isEmpty()) {
                            processQuestions(questionContents, questionsByContent, savedLesson, courseGroup, unitNumber, paperNumber);
                        }

                        // Process materials for this lesson
                        List<Map<String, Object>> lessonMaterials = materialsByTarget.get("lessons");
                        if (lessonMaterials != null) {
                            for (Map<String, Object> materialData : lessonMaterials) {
                                if (lessonName.equals(materialData.get("lessonName"))) {
                                    seedLearningMaterial(materialData, savedLesson, null, null, courseGroup, unitNumber,
                                            paperNumber);
                                }
                            }
                        }

                        // Process materials for questions in this lesson
                        List<Map<String, Object>> questionMaterials = materialsByTarget.get("questions");
                        if (questionMaterials != null && questionContents != null) {
                            for (String quesContent : questionContents) {
                                Question question = questionsByContent.get(quesContent);
                                if (question != null) {
                                    for (Map<String, Object> materialData : questionMaterials) {
                                        if (quesContent.equals(materialData.get("questionContent"))) {
                                            seedLearningMaterial(materialData, null, question, null, courseGroup,
                                                    unitNumber,
                                                    paperNumber);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                System.out.println(">>> END SEEDING: " + savedCourse.getName());
            }
        } catch (IOException e) {
            System.err.println("Error seeding course data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processQuestions(List<String> questionContents, Map<String, Question> questionsByContent,
            Lesson lesson, String courseGroup, String unitNumber, String paperNumber) {
        for (String quesContent : questionContents) {
            Question question = questionsByContent.get(quesContent);
            if (question == null) {
                System.out.println(">>> WARNING: Question not found: " + quesContent);
                continue;
            }

            // Save question without sample answers first (to get question ID)
            for (Question_Choice choice : question.getQuestionChoices()) {
                choice.setQuestion(question);
            }
            
            // Store sample answers temporarily and clear from question
            List<SpeakingSampleAnswer> originalSampleAnswers = question.getSpeakingSampleAnswers();
            question.setSpeakingSampleAnswers(new ArrayList<>());
            
            // Save question first to get the ID
            question = questionRepository.save(question);

            // Process and save sample answers separately for speaking questions
            if (question.getQuesType() == QuestionTypeEnum.SPEAKING &&
                    originalSampleAnswers != null && !originalSampleAnswers.isEmpty()) {

                List<SpeakingSampleAnswer> processedSampleAnswers = new ArrayList<>();
                
                for (SpeakingSampleAnswer sampleAnswer : originalSampleAnswers) {
                    // Set the saved question reference
                    sampleAnswer.setQuestion(question);
                    
                    // Process audio file if audioPath is specified
                    if (sampleAnswer.getAudioLink() != null && !sampleAnswer.getAudioLink().isEmpty()) {
                        String audioPath = sampleAnswer.getAudioLink(); // This contains the source path from JSON
                        String uploadedAudioLink = importSpeakingSampleAudio(audioPath, courseGroup, unitNumber, paperNumber);
                        if (uploadedAudioLink != null) {
                            sampleAnswer.setAudioLink(uploadedAudioLink);
                        } else {
                            // Clear invalid audio path
                            sampleAnswer.setAudioLink(null);
                        }
                    }
                    
                    // Save each sample answer individually
                    SpeakingSampleAnswer savedAnswer = speakingSampleAnswerRepository.save(sampleAnswer);
                    processedSampleAnswers.add(savedAnswer);
                }
                
                // Note: We don't need to restore the sample answers to the question
                // because the relationship is already established via sampleAnswer.setQuestion(question)
                // Restoring the collection would trigger JPA cascade operations that clear audioLink
                
                System.out.println(">>> COMPLETED " + processedSampleAnswers.size() +
                        " sample answers for speaking question: " + question.getQuesContent());
            }

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

    private void seedLearningMaterial(Map<String, Object> materialData, Lesson lesson, Question question,
            Course course, String courseGroup, String testNumber, String paperNumber) {
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

                // Store file in the appropriate course directory
                String uploadPath = String.format("courses/%s/%s/%s",
                        courseGroup.toLowerCase(), testNumber, paperNumber);
                String materLink = learningMaterialService.store(multipartFile, uploadPath);

                // Create Learning_Material record
                Learning_Material material = new Learning_Material();
                material.setMaterLink(materLink);
                material.setMaterType(materType);
                material.setUploadedAt(Instant.now());

                // Set relationships in correct order to avoid mixed IDs
                if (course != null) {
                    material.setCourse(course);
                }
                if (lesson != null) {
                    material.setLesson(lesson);
                }
                if (question != null) {
                    material.setQuestion(question);
                }
                learningMaterialRepository.save(material);

                // Cleanup temp file
                Files.deleteIfExists(tempFile);

                String target;
                if (course != null) {
                    target = "course: " + course.getName();
                } else if (lesson != null) {
                    target = "lesson: " + lesson.getName();
                } else if (question != null) {
                    target = "question: " + question.getQuesContent();
                } else {
                    target = "unknown";
                }
                System.out.println(">>> SEEDED MATERIAL: " + materLink + " for " + target);
            }
        } catch (IOException e) {
            System.err.println("Error seeding learning material: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Import audio file for speaking sample answer
     */
    private String importSpeakingSampleAudio(String audioPath, String courseGroup, String unitNumber, String paperNumber) {
        try {
            // Construct full path to audio file in resources
            String fullAudioPath = String.format("data/%s/file/%s/%s/%s", 
                courseGroup.toLowerCase(), unitNumber, paperNumber, audioPath);
            
            // Load the audio file from resources
            try (InputStream is = new ClassPathResource(fullAudioPath).getInputStream()) {
                // Create temp file with original name
                String fileName = Path.of(audioPath).getFileName().toString();
                Path tempFile = Files.createTempFile("temp_audio_", fileName);
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

                // Determine content type based on file extension
                String contentType = "audio/mpeg"; // default
                String fileExtension = fileName.toLowerCase();
                if (fileExtension.endsWith(".wav")) {
                    contentType = "audio/wav";
                } else if (fileExtension.endsWith(".mp3")) {
                    contentType = "audio/mpeg";
                } else if (fileExtension.endsWith(".m4a")) {
                    contentType = "audio/mp4";
                } else if (fileExtension.endsWith(".ogg")) {
                    contentType = "audio/ogg";
                }

                // Create MultipartFile from the temp file
                MultipartFile multipartFile = new ResourceMultipartFile(
                        audioPath, // name
                        fileName,  // originalFilename
                        contentType,
                        Files.readAllBytes(tempFile));

                // Store file in the speaking-sample-answers directory
                String uploadPath = String.format("speaking-sample-answers/%s/%s/%s",
                        courseGroup.toLowerCase(), unitNumber, paperNumber);
                String audioLink = learningMaterialService.store(multipartFile, uploadPath);

                // Cleanup temp file
                Files.deleteIfExists(tempFile);

                System.out.println(">>> IMPORTED AUDIO: " + audioLink + " from " + fullAudioPath);
                return audioLink;

            } catch (IOException e) {
                System.err.println(">>> ERROR: Could not import audio file: " + fullAudioPath + " - " + e.getMessage());
                return null;
            }

        } catch (Exception e) {
            System.err.println(">>> ERROR: Failed to import speaking sample audio: " + e.getMessage());
            return null;
        }
    }
}