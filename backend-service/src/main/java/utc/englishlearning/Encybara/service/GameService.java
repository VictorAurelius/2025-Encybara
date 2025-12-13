package utc.englishlearning.Encybara.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.GameTypeEnum;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameAnswerRepository gameAnswerRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final UserService userService;

    // === Game Management ===
    
    @Transactional
    public Game createGame(Long courseId, String name, String description, GameTypeEnum gameType, 
                          int maxQuestions, int timeLimit) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
        Game game = new Game();
        game.setName(name);
        game.setDescription(description);
        game.setCourse(course);
        game.setGameType(gameType);
        game.setMaxQuestions(maxQuestions);
        game.setTimeLimit(timeLimit);
        
        return gameRepository.save(game);
    }
    
    @Transactional
    public Game createDefaultReviewGame(Course course) {
        // Tạo game review mặc định cho course
        Game reviewGame = new Game();
        reviewGame.setName("Review: " + course.getName());
        reviewGame.setDescription("Ôn tập toàn bộ nội dung khóa học " + course.getName());
        reviewGame.setCourse(course);
        reviewGame.setGameType(GameTypeEnum.REVIEW);
        reviewGame.setMaxQuestions(15);
        reviewGame.setTimeLimit(600); // 10 phút
        
        return gameRepository.save(reviewGame);
    }
    
    public List<Game> getGamesByCourse(Long courseId) {
        return gameRepository.findActiveByCourseId(courseId);
    }
    
    // === Game Session Management ===
    
    @Transactional
    public GameSession startGame(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        User user = userService.fetchUserById(userId);
        
        // Kiểm tra xem user có session đang chạy không
        Optional<GameSession> activeSession = gameSessionRepository.findByUserAndGame(user, game);
        if (activeSession.isPresent() && !activeSession.get().isCompleted()) {
            return activeSession.get(); // Trả về session đang có
        }
        
        GameSession session = new GameSession(game, user);
        return gameSessionRepository.save(session);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getNextQuestion(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.isCompleted() || session.isExpired()) {
            return Map.of("gameCompleted", true, "message", "Game session has ended");
        }

        // Lấy danh sách questions đã trả lời
        List<GameAnswer> answeredQuestions = gameAnswerRepository.findByGameSessionId(sessionId);
        Set<Long> answeredQuestionIds = answeredQuestions.stream()
            .map(ga -> ga.getQuestion().getId())
            .collect(Collectors.toSet());

        // Lấy random question từ course mà chưa được trả lời
        List<Question> availableQuestions = getRandomQuestionsFromCourse(
            session.getGame().getCourse(), 
            session.getGame().getMaxQuestions(),
            answeredQuestionIds
        );
        
        if (availableQuestions.isEmpty()) {
            // Hoàn thành game
            session.complete();
            gameSessionRepository.save(session);
            return Map.of("gameCompleted", true, "finalScore", session.getScore());
        }
        
        Question question = availableQuestions.get(0);
        
        // Double check: Đảm bảo question là CHOICE type
        if (!QuestionTypeEnum.CHOICE.equals(question.getQuesType())) {
            throw new IllegalStateException("Game only supports CHOICE type questions");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("questionId", question.getId());
        response.put("questionText", question.getQuesContent());
        response.put("questionType", question.getQuesType());
        response.put("skillType", question.getSkillType());
        response.put("questionNumber", answeredQuestions.size() + 1);
        response.put("totalQuestions", session.getGame().getMaxQuestions());
        response.put("timeRemaining", session.getTimeLeft());
        
        // Lấy choices (đã guaranteed có choices từ filter)
        List<Map<String, Object>> choices = question.getQuestionChoices().stream()
            .map(choice -> Map.of(
                "id", choice.getId(),
                "content", (Object) choice.getChoiceContent()
            ))
            .collect(Collectors.toList());
        response.put("choices", choices);
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> submitAnswer(Long sessionId, Long questionId, Long choiceId) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (session.isCompleted() || session.isExpired()) {
            return Map.of("error", "Game session has ended");
        }

        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        
        Question_Choice userChoice = questionChoiceRepository.findById(choiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Choice not found"));

        // Tạo GameAnswer
        GameAnswer gameAnswer = new GameAnswer();
        gameAnswer.setGameSession(session);
        gameAnswer.setQuestion(question);
        gameAnswer.setUserChoice(userChoice);
        gameAnswer.setCorrect(userChoice.isChoiceKey());
        gameAnswer.setQuestionOrder(session.getAnsweredQuestions() + 1);
        
        if (userChoice.isChoiceKey()) {
            gameAnswer.setPointsEarned(question.getPoint());
        }
        
        gameAnswerRepository.save(gameAnswer);
        session.addGameAnswer(gameAnswer);
        session.nextQuestion();
        
        gameSessionRepository.save(session);

        Map<String, Object> result = new HashMap<>();
        result.put("correct", gameAnswer.isCorrect());
        result.put("score", session.getScore());
        result.put("correctAnswerId", getCorrectChoiceId(question));
        result.put("explanation", ""); // Có thể thêm explanation sau
        
        if (session.getAnsweredQuestions() >= session.getGame().getMaxQuestions()) {
            session.complete();
            gameSessionRepository.save(session);
            result.put("gameCompleted", true);
            result.put("finalScore", session.getScore());
            result.put("accuracy", session.getAccuracy());
        }

        return result;
    }
    
    private Long getCorrectChoiceId(Question question) {
        return question.getQuestionChoices().stream()
            .filter(Question_Choice::isChoiceKey)
            .findFirst()
            .map(Question_Choice::getId)
            .orElse(null);
    }
    
    private List<Question> getRandomQuestionsFromCourse(Course course, int maxQuestions, Set<Long> excludeIds) {
        List<Question> allQuestions = new ArrayList<>();
        
        // Lấy tất cả questions từ các Lesson trong Course
        for (Course_Lesson courseLesson : course.getCourselessons()) {
            List<Question> lessonQuestions = questionRepository.findByLesson(courseLesson.getLesson());
            allQuestions.addAll(lessonQuestions);
        }
        
        // Loại bỏ questions đã trả lời, chỉ lấy CHOICE questions và có choices
        List<Question> availableQuestions = allQuestions.stream()
            .filter(q -> !excludeIds.contains(q.getId()))
            .filter(q -> QuestionTypeEnum.CHOICE.equals(q.getQuesType())) // Chỉ lấy câu hỏi trắc nghiệm
            .filter(q -> q.getQuestionChoices() != null && !q.getQuestionChoices().isEmpty())
            .collect(Collectors.toList());
            
        Collections.shuffle(availableQuestions);
        
        return availableQuestions.stream()
            .limit(maxQuestions - excludeIds.size())
            .collect(Collectors.toList());
    }

    @Transactional
    public void endGame(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        if (!session.isCompleted()) {
            session.complete();
            gameSessionRepository.save(session);
        }
    }
    
    @Transactional
    public void updateTimeLeft(Long sessionId, int newTimeLeft) {
        GameSession session = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        session.updateTimeLeft(newTimeLeft);
        gameSessionRepository.save(session);
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
    public Game updateGame(Long gameId, String name, String description, GameTypeEnum gameType, int maxQuestions, int timeLimit) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new ResourceNotFoundException("Game not found."));
        game.setName(name);
        game.setDescription(description);
        game.setGameType(gameType);
        game.setMaxQuestions(maxQuestions);
        game.setTimeLimit(timeLimit);
        return gameRepository.save(game);
    }

    @Transactional
    public void deleteGame(Long gameId) {
        gameRepository.deleteById(gameId);
    }

    @Transactional(readOnly = true)
    public GameSession getGameSession(Long sessionId) {
        return gameSessionRepository.findByIdWithGameAndUser(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    @Transactional(readOnly = true)
    public List<GameSession> getUserGameHistory(Long userId) {
        return gameSessionRepository.findRecentGamesByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
            .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
    }


}