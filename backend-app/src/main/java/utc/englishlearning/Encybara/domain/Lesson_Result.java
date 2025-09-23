package utc.englishlearning.Encybara.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lesson_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "lesson_id", "enrollment_id" })
})
@Getter
@Setter

public class Lesson_Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long stuTime;
    private double comLevel;

    private long sessionId;
    private int totalPoints;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;
}
