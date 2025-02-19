package utc.englishlearning.Encybara.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "answer_texts")
@Getter
@Setter
public class Answer_Text {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String ansContent;
    @OneToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    public void setAnsContent(String[] ansContent) {
        this.ansContent = String.join(", ", ansContent);
    }
}
