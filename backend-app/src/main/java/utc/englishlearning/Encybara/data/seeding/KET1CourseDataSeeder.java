package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.data.loader.JsonDataLoader;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;

import java.io.IOException;

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

    public void seedCourse() {
        try {
            JsonDataLoader.CourseData courseData = jsonDataLoader.loadCourseData("data/ket1-course.json");

            Course existingCourse = courseRepository.findByName(courseData.getName());
            if (existingCourse != null) {
                System.out.println(">>> SKIP: " + courseData.getName() + " already exists");
                return;
            }

            System.out.println(">>> START SEEDING: " + courseData.getName());

            // Create and save course
            Course course = courseData.toCourse();
            course = courseRepository.save(course);

            // Create and save lessons with questions
            for (JsonDataLoader.LessonData lessonData : courseData.getLessons()) {
                // Create and save lesson
                Lesson lesson = lessonData.toLesson();
                lesson = lessonRepository.save(lesson);

                // Create course-lesson relationship
                Course_Lesson courseLesson = new Course_Lesson();
                courseLesson.setCourse(course);
                courseLesson.setLesson(lesson);
                courseLessonRepository.save(courseLesson);

                // Create and save questions with choices
                for (JsonDataLoader.QuestionData questionData : lessonData.getQuestions()) {
                    // Create and save question
                    Question question = questionData.toQuestion();
                    question = questionRepository.save(question);

                    // Create and save choices
                    for (JsonDataLoader.ChoiceData choiceData : questionData.getChoices()) {
                        Question_Choice choice = choiceData.toChoice();
                        choice.setQuestion(question);
                        questionChoiceRepository.save(choice);
                    }

                    // Create lesson-question relationship
                    Lesson_Question lessonQuestion = new Lesson_Question();
                    lessonQuestion.setLesson(lesson);
                    lessonQuestion.setQuestion(question);
                    lessonQuestionRepository.save(lessonQuestion);
                }
            }

            System.out.println(">>> END SEEDING: " + courseData.getName());
        } catch (IOException e) {
            System.err.println("Error loading course data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}