package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.GameAnswer;
import utc.englishlearning.Encybara.domain.GameSession;
import utc.englishlearning.Encybara.domain.Question;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameAnswerRepository extends JpaRepository<GameAnswer, Long> {
    
    // Tìm answers theo session
    List<GameAnswer> findByGameSession(GameSession gameSession);
    
    List<GameAnswer> findByGameSessionId(Long sessionId);
    
    // Tìm answers theo session, sắp xếp theo thứ tự
    @Query("SELECT ga FROM GameAnswer ga WHERE ga.gameSession.id = :sessionId ORDER BY ga.questionOrder ASC")
    List<GameAnswer> findByGameSessionIdOrderByQuestionOrder(@Param("sessionId") Long sessionId);
    
    // Tìm answer cụ thể trong session
    @Query("SELECT ga FROM GameAnswer ga WHERE ga.gameSession.id = :sessionId AND ga.question.id = :questionId")
    Optional<GameAnswer> findByGameSessionIdAndQuestionId(@Param("sessionId") Long sessionId, @Param("questionId") Long questionId);
    
    // Đếm số answers đúng/sai trong session
    @Query("SELECT COUNT(ga) FROM GameAnswer ga WHERE ga.gameSession.id = :sessionId AND ga.isCorrect = :isCorrect")
    long countByGameSessionIdAndIsCorrect(@Param("sessionId") Long sessionId, @Param("isCorrect") boolean isCorrect);
    
    // Tính điểm trung bình theo question
    @Query("SELECT AVG(ga.pointsEarned) FROM GameAnswer ga WHERE ga.question.id = :questionId")
    Double getAveragePointsByQuestionId(@Param("questionId") Long questionId);
    
    // Tìm answers theo user (thông qua session)
    @Query("SELECT ga FROM GameAnswer ga WHERE ga.gameSession.user.id = :userId")
    List<GameAnswer> findByUserId(@Param("userId") Long userId);
    
    // Xóa tất cả answers của session
    void deleteByGameSession(GameSession gameSession);
    
    void deleteByGameSessionId(Long sessionId);
}