package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

@Service
public class KET1CourseDataSeeder {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final LessonQuestionRepository lessonQuestionRepository;

    public KET1CourseDataSeeder(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            CourseLessonRepository courseLessonRepository,
            QuestionChoiceRepository questionChoiceRepository,
            LessonQuestionRepository lessonQuestionRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.courseLessonRepository = courseLessonRepository;
        this.questionChoiceRepository = questionChoiceRepository;
        this.lessonQuestionRepository = lessonQuestionRepository;
    }

    public void seedPresentSimpleCourse() {
        Course existingCourse = courseRepository.findByName("Present Simple Tense");
        if (existingCourse != null) {
            System.out.println(">>> SKIP: Present Simple Course already exists");
            return;
        }

        System.out.println(">>> START SEEDING: Present Simple Course");

        // Create Present Simple course
        Course presentSimpleCourse = new Course();
        presentSimpleCourse.setName("Present Simple Tense");
        presentSimpleCourse.setIntro("Master the basics of Present Simple tense in English");
        presentSimpleCourse.setDiffLevel(1);
        presentSimpleCourse.setRecomLevel(1);
        presentSimpleCourse.setCourseType(CourseTypeEnum.READING);
        presentSimpleCourse.setNumLike(0);
        presentSimpleCourse = courseRepository.save(presentSimpleCourse);

        // Create 5 lessons
        String[] lessonNames = {
                "Affirmative Sentences",
                "Negative Sentences",
                "Interrogative Sentences",
                "Special Forms",
                "Comprehensive Practice"
        };

        for (int i = 0; i < lessonNames.length; i++) {
            Lesson lesson = new Lesson();
            lesson.setName(lessonNames[i]);
            lesson.setSkillType(SkillTypeEnum.READING);
            lesson = lessonRepository.save(lesson);

            // Create Course_Lesson relationship
            Course_Lesson courseLesson = new Course_Lesson();
            courseLesson.setCourse(presentSimpleCourse);
            courseLesson.setLesson(lesson);
            courseLessonRepository.save(courseLesson);

            // Create 10 questions for each lesson
            createQuestionsForLesson(lesson);
        }

        System.out.println(">>> END SEEDING: Present Simple Course");
    }

    private void createQuestionsForLesson(Lesson lesson) {
        QuestionTypeEnum[] questionTypes = { QuestionTypeEnum.CHOICE, QuestionTypeEnum.MULTIPLE,
                QuestionTypeEnum.TEXT };
        String[] questions;

        switch (lesson.getName()) {
            case "Affirmative Sentences":
                questions = new String[] {
                        "Complete the sentence: He ___ to school every day.",
                        "Choose the correct form: I ___ English.",
                        "Fill in: The sun ___ in the east.",
                        "Select the right form: She ___ coffee in the morning.",
                        "Complete: Water ___ at 100 degrees Celsius.",
                        "Choose correct form: Birds ___ in the sky.",
                        "Fill blank: Time ___ fast.",
                        "Select: My father ___ as a doctor.",
                        "Complete: The Earth ___ around the sun.",
                        "Choose: This shop ___ at 9 AM."
                };
                break;
            case "Negative Sentences":
                questions = new String[] {
                        "Complete: He ___ not like spicy food.",
                        "Fill in: I ___ not speak Japanese.",
                        "Choose: She ___ not drink tea.",
                        "Select: They ___ not work on Sundays.",
                        "Complete: It ___ not snow in summer here.",
                        "Fill blank: We ___ not eat meat.",
                        "Choose form: The baby ___ not sleep well.",
                        "Select: The store ___ not open on holidays.",
                        "Complete: He ___ not drive to work.",
                        "Fill: The museum ___ not allow photos."
                };
                break;
            case "Interrogative Sentences":
                questions = new String[] {
                        "___ you speak English?",
                        "___ she like chocolate?",
                        "___ they live in London?",
                        "___ he work at night?",
                        "___ it rain often here?",
                        "___ we need to bring books?",
                        "___ the library open today?",
                        "___ this bus go to the city center?",
                        "___ your parents travel often?",
                        "___ the movie start at 8PM?"
                };
                break;
            case "Special Forms":
                questions = new String[] {
                        "Third person singular: He/She/It ___ (study)",
                        "Verbs ending in -o: He ___ (go)",
                        "Verbs ending in -ch: She ___ (teach)",
                        "Verbs ending in -sh: It ___ (wash)",
                        "Verbs ending in -ss: He ___ (pass)",
                        "Verbs with y->ies: She ___ (try)",
                        "Irregular verbs: He ___ (have)",
                        "Special verb 'be': I ___ (be)",
                        "Special verb 'do': He ___ (do)",
                        "Have/Has usage: She ___ (have)"
                };
                break;
            default: // Comprehensive Practice
                questions = new String[] {
                        "Transform to negative: He plays tennis.",
                        "Make a question: They work hard.",
                        "Correct form: The sun (rise) every day.",
                        "Negative form: She drinks coffee.",
                        "Question form: It costs $5.",
                        "Third person: He (study) English.",
                        "Complete: We (not/go) to school on Sundays.",
                        "Question: You (like) pizza?",
                        "Correct: She (do) homework every day.",
                        "Transform: They (not/speak) Chinese."
                };
        }

        for (int i = 0; i < 10; i++) {
            Question question = new Question();
            question.setQuesContent(questions[i]);
            question.setSkillType(SkillTypeEnum.READING);
            question.setQuesType(questionTypes[i % 3]); // Rotate through question types
            question.setPoint(10);
            question = questionRepository.save(question);

            // Add choices for CHOICE and MULTIPLE type questions
            if (question.getQuesType() == QuestionTypeEnum.CHOICE ||
                    question.getQuesType() == QuestionTypeEnum.MULTIPLE) {
                createChoicesForQuestion(question);
            }

            // Create Lesson_Question relationship
            Lesson_Question lessonQuestion = new Lesson_Question();
            lessonQuestion.setLesson(lesson);
            lessonQuestion.setQuestion(question);
            lessonQuestionRepository.save(lessonQuestion);
        }
    }

    private void createChoicesForQuestion(Question question) {
        String[] choices = new String[4];
        boolean[] isCorrect = new boolean[4];

        // Set choices based on question content
        if (question.getQuesContent().contains("___")) {
            // For fill in the blank questions
            switch (question.getQuesContent().split("___")[0].trim()) {
                case "He":
                case "She":
                case "It":
                    choices = new String[] { "goes", "go", "going", "went" };
                    isCorrect = new boolean[] { true, false, false, false };
                    break;
                case "I":
                case "You":
                case "We":
                case "They":
                    choices = new String[] { "go", "goes", "going", "went" };
                    isCorrect = new boolean[] { true, false, false, false };
                    break;
                default:
                    choices = new String[] { "do", "does", "did", "doing" };
                    isCorrect = new boolean[] { true, false, false, false };
            }
        }

        // Save choices
        for (int i = 0; i < 4; i++) {
            Question_Choice choice = new Question_Choice();
            choice.setQuestion(question);
            choice.setChoiceContent(choices[i]);
            choice.setChoiceKey(isCorrect[i]);
            questionChoiceRepository.save(choice);
        }
    }
}