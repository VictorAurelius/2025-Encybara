package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Course_Lesson;

@Repository
public interface CourseLessonRepository extends JpaRepository<Course_Lesson, Long> {
    void deleteByLesson_IdAndCourse_Id(Long lessonId, Long courseId);
}