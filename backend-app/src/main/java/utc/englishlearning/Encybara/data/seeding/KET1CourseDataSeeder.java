package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

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

    public void seedCourse() {
        Course existingCourse = courseRepository.findByName("Cambridge Key English Test (KET1) - Test 1");
        if (existingCourse != null) {
            System.out.println(">>> SKIP: Cambridge Key English Test (KET1) - Test 1 already exists");
            return;
        }

        System.out.println(">>> START SEEDING: Cambridge Key English Test (KET1) - Test 1");

        Course test1Course = new Course();
        test1Course.setName("Cambridge Key English Test (KET1) - Test 1");
        test1Course.setIntro(
                "(Level A1) Prepare for the Cambridge Key English Test (KET1) with comprehensive practice in reading, writing, listening, and speaking skills. This course covers all essential areas to help you succeed in the KET1 exam.");
        test1Course.setDiffLevel(1.0);
        test1Course.setRecomLevel(1.5);
        test1Course.setCourseType(CourseTypeEnum.ALLSKILLS);
        test1Course.setNumLike(0);
        test1Course.setCourseStatus(CourseStatusEnum.PUBLIC);
        test1Course.setSumLesson(1);
        test1Course.setGroup("KET1");
        test1Course.setSpeciField(SpecialFieldEnum.EVERYONE);
        test1Course.setCreateBy("admin");
        test1Course = courseRepository.save(test1Course);

        Lesson part1Paper1 = new Lesson();
        part1Paper1.setName("Paper 1: Reading and Writing - Part 1");
        part1Paper1.setSkillType(SkillTypeEnum.READING);
        part1Paper1.setSumQues(5);
        part1Paper1.setCreateBy("admin");
        part1Paper1 = lessonRepository.save(part1Paper1);

        Course_Lesson test1P1 = new Course_Lesson();
        test1P1.setCourse(test1Course);
        test1P1.setLesson(part1Paper1);
        courseLessonRepository.save(test1P1);

        // Create 10 questions for each lesson
        createQuestionsForPart1Paper1(part1Paper1);

        Lesson part2Paper1 = new Lesson();
        part2Paper1.setName("Paper 1: Reading and Writing - Part 2");
        part2Paper1.setSkillType(SkillTypeEnum.READING);
        part2Paper1.setSumQues(5);
        part2Paper1.setCreateBy("admin");
        part2Paper1 = lessonRepository.save(part2Paper1);

        Course_Lesson test2P1 = new Course_Lesson();
        test2P1.setCourse(test1Course);
        test2P1.setLesson(part2Paper1);
        courseLessonRepository.save(test2P1);

        // Create 10 questions for each lesson
        createQuestionsForPart2Paper1(part2Paper1);

        System.out.println(">>> END SEEDING: Cambridge Key English Test (KET1) - Test 1");
    }

    private void createQuestionsForPart1Paper1(Lesson lesson) {

        Question ques1 = new Question();
        ques1.setQuesContent("You can't drive this way.");
        ques1.setSkillType(SkillTypeEnum.READING);
        ques1.setQuesType(QuestionTypeEnum.TEXT);
        ques1.setPoint(10);
        ques1.setCreateBy("admin");
        ques1 = questionRepository.save(ques1);

        Question_Choice ques1Choice1 = new Question_Choice();
        ques1Choice1.setChoiceContent("F");
        ques1Choice1.setChoiceKey(true);
        questionChoiceRepository.save(ques1Choice1);

        Lesson_Question ques1les = new Lesson_Question();
        ques1les.setLesson(lesson);
        ques1les.setQuestion(ques1);
        lessonQuestionRepository.save(ques1les);

        Question ques2 = new Question();
        ques2.setQuesContent("Children do not have to pay.");
        ques2.setSkillType(SkillTypeEnum.READING);
        ques2.setQuesType(QuestionTypeEnum.TEXT);
        ques2.setPoint(10);
        ques2.setCreateBy("admin");
        ques2 = questionRepository.save(ques2);

        Question_Choice ques2Choice1 = new Question_Choice();
        ques2Choice1.setChoiceContent("A");
        ques2Choice1.setChoiceKey(true);
        questionChoiceRepository.save(ques2Choice1);

        Lesson_Question ques2les = new Lesson_Question();
        ques2les.setLesson(lesson);
        ques2les.setQuestion(ques1);
        lessonQuestionRepository.save(ques2les);

    }

    private void createQuestionsForPart2Paper1(Lesson lesson) {
    }
}