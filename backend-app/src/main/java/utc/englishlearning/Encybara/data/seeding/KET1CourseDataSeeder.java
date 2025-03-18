package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.data.loader.JsonDataLoader;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class KET1CourseDataSeeder {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final LessonQuestionRepository lessonQuestionRepository;
    private final JsonDataLoader jsonDataLoader;

    public KET1CourseDataSeeder(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            CourseLessonRepository courseLessonRepository,
            QuestionChoiceRepository questionChoiceRepository,
            LessonQuestionRepository lessonQuestionRepository,
            JsonDataLoader jsonDataLoader) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.courseLessonRepository = courseLessonRepository;
        this.questionChoiceRepository = questionChoiceRepository;
        this.lessonQuestionRepository = lessonQuestionRepository;
        this.jsonDataLoader = jsonDataLoader;
    }

    @PostConstruct
    public void seedData() {
        try {
            // Load all data first
            List<Course> courses = jsonDataLoader.loadCourses();
            Map<String, Lesson> lessonMap = jsonDataLoader.loadLessons();
            Map<String, Question> questionMap = jsonDataLoader.loadQuestions();

            for (Course course : courses) {
                // Check if course already exists
                Course existingCourse = courseRepository.findByName(course.getName());
                if (existingCourse != null) {
                    System.out.println(">>> SKIP: " + course.getName() + " already exists");
                    continue;
                }

                System.out.println(">>> START SEEDING: " + course.getName());

                // Save course
                course = courseRepository.save(course);

                // Save lessons and create relationships
                for (Map.Entry<String, Lesson> entry : lessonMap.entrySet()) {
                    Lesson lesson = entry.getValue();

                    // Save lesson
                    lesson = lessonRepository.save(lesson);

                    // Create course-lesson relationship
                    Course_Lesson courseLesson = new Course_Lesson();
                    courseLesson.setCourse(course);
                    courseLesson.setLesson(lesson);
                    courseLessonRepository.save(courseLesson);

                    // Find corresponding questions for this lesson from JSON data
                    String lessonId = entry.getKey();
                    for (Map.Entry<String, Question> qEntry : questionMap.entrySet()) {
                        Question question = qEntry.getValue();

                        // Save question
                        question = questionRepository.save(question);

                        // Create lesson-question relationship
                        Lesson_Question lessonQuestion = new Lesson_Question();
                        lessonQuestion.setLesson(lesson);
                        lessonQuestion.setQuestion(question);
                        lessonQuestionRepository.save(lessonQuestion);

                        // Save question choices
                        for (Question_Choice choice : question.getQuestionChoices()) {
                            choice.setQuestion(question);
                            questionChoiceRepository.save(choice);
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
}