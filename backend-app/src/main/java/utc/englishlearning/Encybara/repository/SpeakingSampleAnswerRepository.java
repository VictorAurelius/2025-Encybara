package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.SpeakingSampleAnswer;

import java.util.List;

@Repository
public interface SpeakingSampleAnswerRepository extends JpaRepository<SpeakingSampleAnswer, Long> {

    /**
     * Find all sample answers for a specific question
     * 
     * @param questionId the question ID
     * @return list of sample answers
     */
    @Query("SELECT s FROM SpeakingSampleAnswer s WHERE s.question.id = :questionId ORDER BY s.difficultyLevel ASC, s.estimatedScore DESC")
    List<SpeakingSampleAnswer> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * Find sample answers by difficulty level for a specific question
     * 
     * @param questionId      the question ID
     * @param difficultyLevel the difficulty level (1-5)
     * @return list of sample answers
     */
    @Query("SELECT s FROM SpeakingSampleAnswer s WHERE s.question.id = :questionId AND s.difficultyLevel = :difficultyLevel ORDER BY s.estimatedScore DESC")
    List<SpeakingSampleAnswer> findByQuestionIdAndDifficultyLevel(@Param("questionId") Long questionId,
            @Param("difficultyLevel") Integer difficultyLevel);

    /**
     * Count sample answers for a specific question
     * 
     * @param questionId the question ID
     * @return count of sample answers
     */
    @Query("SELECT COUNT(s) FROM SpeakingSampleAnswer s WHERE s.question.id = :questionId")
    Long countByQuestionId(@Param("questionId") Long questionId);

    /**
     * Check if question has any sample answers
     * 
     * @param questionId the question ID
     * @return true if question has sample answers
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SpeakingSampleAnswer s WHERE s.question.id = :questionId")
    boolean existsByQuestionId(@Param("questionId") Long questionId);

    /**
     * Delete all sample answers for a specific question
     * 
     * @param questionId the question ID
     */
    @Query("DELETE FROM SpeakingSampleAnswer s WHERE s.question.id = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
}