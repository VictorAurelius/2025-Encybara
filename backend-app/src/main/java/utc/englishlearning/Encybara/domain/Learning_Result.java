package utc.englishlearning.Encybara.domain;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="learning_results")
@Getter
@Setter
public class Learning_Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int pointReading;
    private int pointListening;
    private int pointWritting;
    private int pointSpeaking;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
