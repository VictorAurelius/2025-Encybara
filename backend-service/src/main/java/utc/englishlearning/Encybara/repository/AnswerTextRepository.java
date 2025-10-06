package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import utc.englishlearning.Encybara.domain.Answer_Text;
import utc.englishlearning.Encybara.domain.Answer;

import java.util.Optional;

public interface AnswerTextRepository extends JpaRepository<Answer_Text, Long> {
    Optional<Answer_Text> findByAnswer(Answer answer);
}