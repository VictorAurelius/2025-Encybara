package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.domain.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    // Additional query methods can be defined here

    @Query("SELECT q FROM Question q JOIN Lesson_Question lq ON q.id = lq.question.id WHERE lq.lesson.id = :lessonId")
    List<Question> findByLessonId(@Param("lessonId") Long lessonId);

    @Query("SELECT q FROM Question q JOIN Lesson_Question lq ON q.id = lq.question.id WHERE lq.lesson = :lesson")
    List<Question> findByLesson(@Param("lesson") Lesson lesson);
}