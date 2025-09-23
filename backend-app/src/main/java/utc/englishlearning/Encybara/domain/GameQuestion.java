package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_questions")
public class GameQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(name = "text", nullable = false)
    private String text;

    @ElementCollection
    @CollectionTable(name = "game_question_choices", 
                    joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "choice")
    private List<String> choices;

    @Column(nullable = false)
    private String correctAnswer;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean answered = false;

    @Column
    private String userAnswer;

    @Column
    private LocalDateTime answeredAt;

    @Column(nullable = false)
    private int points = 10;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public GameQuestion(String text, List<String> choices, String correctAnswer) {
        this.text = text;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
    }

    public boolean isCorrectAnswer(String answer) {
        return correctAnswer != null && correctAnswer.equals(answer);
    }

    public void setAnswer(String answer) {
        this.userAnswer = answer;
        this.answered = true;
        this.answeredAt = LocalDateTime.now();
    }

    public boolean getAnswered() {
        return answered;
    }

    public int getPoints() {
        return points;
    }
}