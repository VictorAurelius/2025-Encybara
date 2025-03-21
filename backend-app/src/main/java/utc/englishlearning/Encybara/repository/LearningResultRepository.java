package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Learning_Result;
import utc.englishlearning.Encybara.domain.User;

import java.util.Optional;
import java.util.List;

@Repository
public interface LearningResultRepository extends JpaRepository<Learning_Result, Long> {
    Optional<Learning_Result> findByUser(User user);

    @Query("SELECT lr FROM Learning_Result lr " +
            "LEFT JOIN FETCH lr.enrollmentHistory eh " +
            "WHERE lr.user.id = :userId " +
            "ORDER BY eh.errolDate DESC")
    Optional<Learning_Result> findByUserIdWithHistory(@Param("userId") Long userId);

    @Query("SELECT lr FROM Learning_Result lr " +
            "LEFT JOIN FETCH lr.enrollmentHistory eh " +
            "WHERE lr.user.id = :userId " +
            "AND eh.errolDate >= CURRENT_DATE - 30 " +
            "ORDER BY eh.errolDate DESC")
    Optional<Learning_Result> findByUserIdWithRecentHistory(@Param("userId") Long userId);

    @Query("SELECT e.course.diffLevel, AVG(e.comLevel) as avgCompletion " +
            "FROM Enrollment e " +
            "WHERE e.learningResult.id = :learningResultId " +
            "GROUP BY e.course.diffLevel " +
            "ORDER BY e.course.diffLevel DESC")
    List<Object[]> findCompletionRatesByDifficulty(@Param("learningResultId") Long learningResultId);
}