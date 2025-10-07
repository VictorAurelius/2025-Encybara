package utc.englishlearning.Encybara.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utc.englishlearning.Encybara.domain.GameSession;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionStatusResponse {
    
    private Long id;
    private Long gameId;
    private String gameName;
    private String gameDescription;
    private Long userId;
    private String userName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int score;
    private int totalQuestions;
    private int correctAnswers;
    private boolean completed;
    private int currentQuestionNumber;
    private long timeRemaining;
    private double accuracy;
    private int answeredQuestions;
    private boolean expired;

    public static GameSessionStatusResponse fromGameSession(GameSession session) {
        GameSessionStatusResponse response = new GameSessionStatusResponse();
        response.setId(session.getId());
        response.setGameId(session.getGame().getId());
        response.setGameName(session.getGame().getName());
        response.setGameDescription(session.getGame().getDescription());
        response.setUserId(session.getUser().getId());
        response.setUserName(session.getUser().getEmail()); // hoặc getFullName() nếu có
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setScore(session.getScore());
        response.setTotalQuestions(session.getTotalQuestions());
        response.setCorrectAnswers(session.getCorrectAnswers());
        response.setCompleted(session.isCompleted());
        response.setCurrentQuestionNumber(session.getCurrentQuestionNumber());
        response.setTimeRemaining(session.getTimeLeft());
        response.setAccuracy(session.getAccuracy());
        response.setAnsweredQuestions(session.getAnsweredQuestions());
        response.setExpired(session.isExpired());
        
        return response;
    }
}