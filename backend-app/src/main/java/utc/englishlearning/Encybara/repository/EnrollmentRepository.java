package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    Page<Enrollment> findByUserIdAndProStatus(Long userId, Boolean proStatus, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId ORDER BY e.errolDate DESC, e.id DESC")
    Optional<Enrollment> findFirstByCourseIdOrderByErrolDateDesc(@Param("courseId") Long courseId);

    @Query(value = "SELECT e FROM Enrollment e WHERE e.course.id = :courseId ORDER BY e.errolDate DESC")
    List<Enrollment> findTopByCourseIdOrderByErrolDateDesc(@Param("courseId") Long courseId, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.user.id = :userId ORDER BY e.errolDate DESC")
    List<Enrollment> findTopByCourseIdAndUserIdOrderByErrolDateDesc(
            @Param("courseId") Long courseId,
            @Param("userId") Long userId,
            Pageable pageable);
}