package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Lesson_Question;

@Repository
public interface LessonQuestionRepository extends JpaRepository<Lesson_Question, Long> {
    void deleteByLessonIdAndQuestionId(Long lessonId, Long questionId);
}