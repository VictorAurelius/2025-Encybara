package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Lesson_Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import utc.englishlearning.Encybara.domain.Enrollment;

@Repository
public interface LessonResultRepository extends JpaRepository<Lesson_Result, Long> {
        @Query("SELECT DISTINCT lr FROM Lesson_Result lr WHERE lr.lesson.id = :lessonId")
        Page<Lesson_Result> findByLessonId(@Param("lessonId") Long lessonId, Pageable pageable);

        @Query("SELECT DISTINCT lr FROM Lesson_Result lr WHERE lr.user.id = :userId AND lr.lesson.id = :lessonId")
        Page<Lesson_Result> findByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId,
                        Pageable pageable);

        @Query("SELECT DISTINCT lr FROM Lesson_Result lr WHERE lr.user.id = :userId ORDER BY lr.sessionId DESC")
        Page<Lesson_Result> findByUserIdOrderBySessionIdDesc(@Param("userId") Long userId, Pageable pageable);

        @Query("SELECT DISTINCT lr FROM Lesson_Result lr WHERE lr.user.id = :userId AND lr.lesson.id = :lessonId ORDER BY lr.sessionId DESC")
        List<Lesson_Result> findByUserIdAndLessonIdOrderBySessionIdDesc(@Param("userId") Long userId,
                        @Param("lessonId") Long lessonId);

        @Query("SELECT DISTINCT lr FROM Lesson_Result lr WHERE lr.enrollment = :enrollment")
        List<Lesson_Result> findByEnrollment(@Param("enrollment") Enrollment enrollment);

        boolean existsByUserIdAndLessonIdAndSessionIdAndEnrollmentId(Long userId, Long lessonId, Long sessionId,
                        Long enrollmentId);

        Lesson_Result findByLessonIdAndEnrollmentId(Long lessonId, Long enrollmentId);

        boolean existsByLessonIdAndEnrollmentId(Long lessonId, Long enrollmentId);
}