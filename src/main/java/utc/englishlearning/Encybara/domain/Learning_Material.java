package utc.englishlearning.Encybara.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "learning_materials")
@Getter
@Setter
public class Learning_Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String materType;
    private String materLink;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = true)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = true)
    private Lesson lesson;
}
