package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import utc.englishlearning.Encybara.domain.Question_Choice;
import java.util.List;

public interface QuestionChoiceRepository extends JpaRepository<Question_Choice, Long> {
    List<Question_Choice> findByQuestionId(Long questionId);
}