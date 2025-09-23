package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.GameQuestion;
import utc.englishlearning.Encybara.domain.GameSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameQuestionRepository extends JpaRepository<GameQuestion, Long> {

    @Query("SELECT q FROM GameQuestion q WHERE q.gameSession = :session ORDER BY q.createdAt")
    List<GameQuestion> findBySession(@Param("session") GameSession session);

    @Query("SELECT q FROM GameQuestion q WHERE q.gameSession.id = :sessionId ORDER BY q.createdAt")
    List<GameQuestion> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(q) FROM GameQuestion q WHERE q.gameSession.id = :sessionId")
    Long countBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(q) FROM GameQuestion q WHERE q.gameSession = :session")
    Long countBySession(@Param("session") GameSession session);

    @Query("""
        SELECT q FROM GameQuestion q 
        WHERE q.gameSession.id = :sessionId 
        AND q.createdAt > (SELECT q2.createdAt FROM GameQuestion q2 WHERE q2.id = :lastQuestionId)
        ORDER BY q.createdAt ASC
    """)
    List<GameQuestion> findNextQuestions(@Param("sessionId") Long sessionId, @Param("lastQuestionId") Long lastQuestionId);

    @Query("SELECT q FROM GameQuestion q WHERE q.gameSession.id = :sessionId AND q.id = :questionId")
    Optional<GameQuestion> findBySessionIdAndQuestionId(@Param("sessionId") Long sessionId, @Param("questionId") Long questionId);

    void deleteByGameSession(GameSession session);

    void deleteByGameSessionId(Long sessionId);
}