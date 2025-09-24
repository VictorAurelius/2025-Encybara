package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game; // Reference đến Game thay vì lưu questions riêng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GameAnswer> gameAnswers = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int totalQuestions = 0; // Tổng số câu hỏi trong session này

    @Column(nullable = false)
    private int correctAnswers = 0; // Số câu trả lời đúng

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private int currentQuestionIndex = 0;

    @Column(name = "time_limit", nullable = false)
    private int timeLimit = 300; // Default 5 minutes, will be set from Game

    public GameSession(Game game, User user) {
        this.game = game;
        this.user = user;
        this.startTime = LocalDateTime.now();
        this.totalQuestions = game.getMaxQuestions();
        this.timeLimit = game.getTimeLimit(); // Copy time limit from Game
    }

    public void addGameAnswer(GameAnswer gameAnswer) {
        gameAnswers.add(gameAnswer);
        gameAnswer.setGameSession(this);
        
        if (gameAnswer.isCorrect()) {
            this.correctAnswers++;
        }
        this.score += gameAnswer.getPointsEarned();
    }

    public void incrementScore(int points) {
        this.score += points;
    }

    public long getTimeLeft() {
        if (completed) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = ChronoUnit.SECONDS.between(startTime, now);
        long timeLeft = this.timeLimit - secondsElapsed;
        
        return Math.max(0, timeLeft);
    }

    public boolean isExpired() {
        return getTimeLeft() <= 0;
    }

    public void complete() {
        this.completed = true;
        this.endTime = LocalDateTime.now();
    }

    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }

    public void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex >= totalQuestions) {
            complete();
        }
    }

    public void updateTimeLeft(int newTimeLeft) {
        // This method might not be needed with new design
        // since time limit is managed by Game entity
        if (newTimeLeft <= 0) {
            complete();
        }
    }
    
    public boolean hasNextQuestion() {
        return currentQuestionIndex < totalQuestions - 1;
    }

    public double getAccuracy() {
        if (gameAnswers.isEmpty()) {
            return 0.0;
        }
        return (double) correctAnswers / gameAnswers.size() * 100;
    }

    public int getAnsweredQuestions() {
        return gameAnswers.size();
    }
}