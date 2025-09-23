package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
}