package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.GameSession;
import utc.englishlearning.Encybara.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    @Query("SELECT g FROM GameSession g WHERE g.user = :user AND g.completed = false")
    Optional<GameSession> findByUserAndActiveTrue(@Param("user") User user);

    @Query("SELECT AVG(g.score) FROM GameSession g WHERE g.user = :user AND g.completed = true")
    Double getAverageScoreByUser(@Param("user") User user);

    @Query("SELECT g FROM GameSession g WHERE g.user = :user AND g.completed = true ORDER BY g.score DESC")
    List<GameSession> findByUserOrderByScoreDesc(@Param("user") User user);

    @Query("SELECT g FROM GameSession g WHERE g.user.id = :userId AND g.completed = true ORDER BY g.startTime DESC")
    List<GameSession> findRecentGamesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(g) FROM GameSession g WHERE g.user.id = :userId AND g.completed = true")
    Long countCompletedGamesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT g FROM GameSession g WHERE g.score >= :minScore AND g.completed = true")
    List<GameSession> findHighScoreSessions(@Param("minScore") Integer minScore);

    @Query("SELECT g FROM GameSession g WHERE g.user.id = :userId AND g.completed = true AND g.score > 0 ORDER BY g.score DESC")
    List<GameSession> findUserTopScores(@Param("userId") Long userId);

    @Query("""
        SELECT g FROM GameSession g 
        WHERE g.completed = true 
        AND g.score > 0 
        ORDER BY g.score DESC, g.endTime ASC
    """)
    List<GameSession> findGlobalLeaderboard();
}