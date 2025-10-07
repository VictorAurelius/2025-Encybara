package utc.englishlearning.Encybara.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import utc.englishlearning.Encybara.domain.Answer;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.User;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByUserAndQuestion(User user, Question question);

    List<Answer> findByUserAndQuestionInAndSessionId(User user, List<Question> questions, long sessionId);
}