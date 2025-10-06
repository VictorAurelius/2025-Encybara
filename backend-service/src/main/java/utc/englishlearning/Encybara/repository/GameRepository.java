package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.domain.Game;
import utc.englishlearning.Encybara.util.constant.GameTypeEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    // Tìm games theo course
    List<Game> findByCourse(Course course);
    
    List<Game> findByCourseId(Long courseId);
    
    // Tìm games active theo course
    @Query("SELECT g FROM Game g WHERE g.course.id = :courseId AND g.isActive = true")
    List<Game> findActiveByCourseId(@Param("courseId") Long courseId);
    
    // Tìm game theo type và course
    List<Game> findByCourseAndGameType(Course course, GameTypeEnum gameType);
    
    @Query("SELECT g FROM Game g WHERE g.course.id = :courseId AND g.gameType = :gameType AND g.isActive = true")
    List<Game> findByCourseIdAndGameType(@Param("courseId") Long courseId, @Param("gameType") GameTypeEnum gameType);
    
    // Tìm game mặc định (REVIEW) cho course
    @Query("SELECT g FROM Game g WHERE g.course.id = :courseId AND g.gameType = 'REVIEW' AND g.isActive = true")
    Optional<Game> findDefaultReviewGameByCourseId(@Param("courseId") Long courseId);
    
    // Tìm tất cả games active
    List<Game> findByIsActiveTrue();
    
    // Tìm games theo name
    @Query("SELECT g FROM Game g WHERE g.name LIKE %:name% AND g.isActive = true")
    List<Game> findByNameContainingAndIsActiveTrue(@Param("name") String name);
}