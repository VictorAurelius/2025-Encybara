package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_answers")
@Getter
@Setter
public class GameAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // Reference đến Question từ Course
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_choice_id", nullable = true)
    private Question_Choice userChoice; // Lựa chọn của user
    
    @Column(nullable = false)
    private boolean isCorrect = false;
    
    @Column(nullable = false)
    private int pointsEarned = 0;
    
    @Column(nullable = false)
    private LocalDateTime answeredAt;
    
    @Column(nullable = false)
    private int timeSpent = 0; // seconds spent on this question
    
    @Column(nullable = false)
    private int questionOrder; // Thứ tự câu hỏi trong game session
    
    @PrePersist
    public void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }
}