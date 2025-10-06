package utc.englishlearning.Encybara.domain;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "learning_results")
@Getter
@Setter
public class Learning_Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double listeningScore;
    private double speakingScore;
    private double readingScore;
    private double writingScore;

    private Instant lastUpdated;
    private double previousListeningScore;
    private double previousSpeakingScore;
    private double previousReadingScore;
    private double previousWritingScore;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "learningResult")
    private List<Enrollment> enrollmentHistory = new ArrayList<>();
}
