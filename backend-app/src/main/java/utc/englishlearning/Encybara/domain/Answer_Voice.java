package utc.englishlearning.Encybara.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "answer_voices")
@Getter
@Setter
public class Answer_Voice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; 

    private String voiceLink; 

    @OneToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;
}
