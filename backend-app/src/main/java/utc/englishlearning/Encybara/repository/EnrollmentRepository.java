package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Enrollment;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
        Page<Enrollment> findByUserId(Long userId, Pageable pageable);

        Page<Enrollment> findByUserIdAndProStatus(Long userId, Boolean proStatus, Pageable pageable);

        List<Enrollment> findTopByCourseIdAndUserIdOrderByErrolDateDesc(Long courseId, Long userId, Pageable pageable);

        @Query("SELECT e FROM Enrollment e " +
                        "JOIN e.course c " +
                        "WHERE e.user.id = :userId " +
                        "AND c.courseType = :courseType")
        Page<Enrollment> findByUserIdAndCourseTypeSortedByEnrollDate(
                        @Param("userId") Long userId,
                        @Param("courseType") CourseTypeEnum courseType,
                        Pageable pageable);

        @Query("SELECT e FROM Enrollment e " +
                        "JOIN e.course c " +
                        "WHERE e.user.id = :userId " +
                        "AND c.courseType = :courseType " +
                        "AND e.comLevel >= :minCompletion")
        Page<Enrollment> findSuccessfulEnrollments(
                        @Param("userId") Long userId,
                        @Param("courseType") CourseTypeEnum courseType,
                        @Param("minCompletion") double minCompletion,
                        Pageable pageable);

        @Query("SELECT AVG(e.comLevel) FROM Enrollment e " +
                        "JOIN e.course c " +
                        "WHERE e.user.id = :userId " +
                        "AND c.courseType = :courseType " +
                        "AND e.errolDate >= CURRENT_DATE - 30")
        Double getAverageCompletionRateLastMonth(
                        @Param("userId") Long userId,
                        @Param("courseType") CourseTypeEnum courseType);
}