package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.domain.Enrollment;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
        Page<Enrollment> findByUserId(Long userId, Pageable pageable);

        Page<Enrollment> findByUserIdAndProStatus(Long userId, Boolean proStatus, Pageable pageable);

        List<Enrollment> findTopByCourseIdAndUserIdOrderByEnrollDateDesc(Long courseId, Long userId, Pageable pageable);

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

        @Query(value = "SELECT COALESCE(AVG(e.com_level), 0.0) " +
                        "FROM enrollments e " +
                        "JOIN courses c ON e.course_id = c.id " +
                        "WHERE e.user_id = :userId " +
                        "AND c.course_type = :courseType " +
                        "AND e.enroll_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)", nativeQuery = true)
        Double getAverageCompletionRateLastMonth(
                        @Param("userId") Long userId,
                        @Param("courseType") String courseType);

        void deleteByUserAndProStatusFalse(User user);

        boolean existsByUserAndCourseAndProStatusTrue(User user, Course course);

        /**
         * Find enrollment by user ID and course ID
         * 
         * @param userId   the ID of the user
         * @param courseId the ID of the course
         * @return Optional containing the enrollment if found
         */
        Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
}