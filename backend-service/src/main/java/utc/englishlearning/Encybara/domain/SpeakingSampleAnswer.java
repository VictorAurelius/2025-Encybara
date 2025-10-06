package utc.englishlearning.Encybara.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.SecurityUtil;

import java.time.Instant;

@Entity
@Table(name = "speaking_sample_answers")
@Getter
@Setter
public class SpeakingSampleAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String answerContent;

    @Column(length = 500)
    private String description;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 1-5 (1=Basic, 2=Elementary, 3=Intermediate, 4=Advanced, 5=Expert)

    @Column(name = "estimated_score")
    private Double estimatedScore; // Expected score for this answer (0-100)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;

    // Audit fields
    private String createBy;
    private Instant createAt;
    private String updateBy;
    private Instant updateAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.updateAt = Instant.now();
    }
}