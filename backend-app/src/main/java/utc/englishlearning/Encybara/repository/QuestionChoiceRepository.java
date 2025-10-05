package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utc.englishlearning.Encybara.domain.Question_Choice;
import java.util.List;

public interface QuestionChoiceRepository extends JpaRepository<Question_Choice, Long> {
    List<Question_Choice> findByQuestionId(Long questionId);

    /**
     * Count question choices for a specific question
     *
     * @param questionId the question ID
     * @return count of question choices
     */
    @Query("SELECT COUNT(q) FROM Question_Choice q WHERE q.question.id = :questionId")
    long countByQuestionId(@Param("questionId") Long questionId);
}