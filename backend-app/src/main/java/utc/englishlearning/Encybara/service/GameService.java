package utc.englishlearning.Encybara.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.GameQuestion;
import utc.englishlearning.Encybara.domain.GameSession;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.repository.GameQuestionRepository;
import utc.englishlearning.Encybara.repository.GameSessionRepository;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final GameQuestionRepository questionRepository;
    private final UserService userService;

    @Transactional
    public GameSession startGame(Long userId) {
        User user = userService.fetchUserById(userId);
        
        // Check if user already has an active session
        Optional<GameSession> existingSession = gameSessionRepository.findByUserAndActiveTrue(user);
        if (existingSession.isPresent()) {
            GameSession session = existingSession.get();
            session.complete(); // End the previous session
            gameSessionRepository.save(session);
        }

        // Create new session
        GameSession session = new GameSession(user);
        
        // Generate questions for the session
        List<GameQuestion> questions = generateQuestions();
        questions.forEach(session::addQuestion);
        
        return gameSessionRepository.save(session);
    }

    private List<GameQuestion> generateQuestions() {
        List<String> sampleWords = Arrays.asList(
            "apple", "banana", "cherry", "date", "elderberry",
            "fig", "grape", "honeydew", "kiwi", "lemon"
        );
        
        List<GameQuestion> questions = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            String correctAnswer = sampleWords.get(random.nextInt(sampleWords.size()));
            List<String> choices = generateChoices(sampleWords, correctAnswer);
            
            GameQuestion question = new GameQuestion(
                "What is the correct spelling of this word: " + correctAnswer + "?",
                choices,
                correctAnswer
            );
            questions.add(question);
        }
        
        return questions;
    }

    private List<String> generateChoices(List<String> words, String correctAnswer) {
        List<String> choices = new ArrayList<>();
        choices.add(correctAnswer);
        
        Random random = new Random();
        while (choices.size() < 4) {
            String word = words.get(random.nextInt(words.size()));
            if (!choices.contains(word)) {
                choices.add(word);
            }
        }
        
        Collections.shuffle(choices);
        return choices;
    }

    @Transactional
    public GameQuestion getNextQuestion(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.isCompleted() || session.getTimeLeft() <= 0) {
            return null;
        }

        return session.getCurrentQuestion();
    }

    @Transactional
    public Map<String, Object> submitAnswer(Long sessionId, String answer) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        Map<String, Object> result = new HashMap<>();
        
        if (session.isCompleted() || session.getTimeLeft() <= 0) {
            result.put("error", "Game session has ended");
            return result;
        }

        GameQuestion currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null) {
            result.put("error", "No current question");
            return result;
        }

        boolean correct = currentQuestion.isCorrectAnswer(answer);
        if (correct) {
            session.incrementScore(currentQuestion.getPoints());
        }

        result.put("correct", correct);
        result.put("score", session.getScore());
        result.put("correctAnswer", currentQuestion.getCorrectAnswer());

        session.nextQuestion();
        if (session.getCurrentQuestion() == null) {
            session.complete();
        }

        gameSessionRepository.save(session);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLeaderboard(Long userId) {
        User user = userService.fetchUserById(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("userScore", gameSessionRepository.getAverageScoreByUser(user));
        result.put("topScores", gameSessionRepository.findByUserOrderByScoreDesc(user)
            .stream()
            .limit(10)
            .map(GameSession::getScore)
            .toList());
            
        return result;
    }

    @Transactional
    public void updateTimeLeft(Long sessionId, int newTimeLeft) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            
        session.updateTimeLeft(newTimeLeft);
        gameSessionRepository.save(session);
    }

    @Transactional
    public void endGame(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            
        session.complete();
        gameSessionRepository.save(session);
    }
}