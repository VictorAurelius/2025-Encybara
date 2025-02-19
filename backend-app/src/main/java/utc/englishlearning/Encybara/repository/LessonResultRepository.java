package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Lesson_Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import utc.englishlearning.Encybara.domain.Enrollment;

@Repository
public interface LessonResultRepository extends JpaRepository<Lesson_Result, Long> {
    Page<Lesson_Result> findByLessonId(Long lessonId, Pageable pageable);

    Page<Lesson_Result> findByUserIdAndLessonId(Long userId, Long lessonId, Pageable pageable);

    Page<Lesson_Result> findByUserIdOrderBySessionIdDesc(Long userId, Pageable pageable);

    List<Lesson_Result> findByUserIdAndLessonIdOrderBySessionIdDesc(Long userId, Long lessonId);

    List<Lesson_Result> findByEnrollment(Enrollment enrollment);

    boolean existsByUserIdAndLessonIdAndSessionIdAndEnrollmentId(Long userId, Long lessonId, Long sessionId,
            Long enrollmentId);
}