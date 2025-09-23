package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameQuestion> questions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int timeLimit = 45; // seconds

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private int currentQuestionIndex = 0;

    public GameSession(User user) {
        this.user = user;
        this.startTime = LocalDateTime.now();
    }

    public void addQuestion(GameQuestion question) {
        questions.add(question);
        question.setGameSession(this);
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
        long timeLeft = timeLimit - secondsElapsed;
        
        return Math.max(0, timeLeft);
    }

    public boolean isExpired() {
        return getTimeLeft() <= 0;
    }

    public void complete() {
        this.completed = true;
        this.endTime = LocalDateTime.now();
    }

    public GameQuestion getCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }

    public void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex >= questions.size()) {
            complete();
        }
    }

    public void updateTimeLeft(int newTimeLeft) {
        this.timeLimit = Math.max(0, newTimeLeft);
        if (this.timeLimit == 0) {
            complete();
        }
    }
    
    public boolean hasNextQuestion() {
        return currentQuestionIndex < questions.size() - 1;
    }
}