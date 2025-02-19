package utc.englishlearning.Encybara.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private Instant uploadedAt;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "question_id", nullable = true)
    private Question question;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "lesson_id", nullable = true)
    private Lesson lesson;

    public void setLessonId(Long lessonId) {
        this.lesson = new Lesson();
        this.lesson.setId(lessonId);
    }

    public void setQuestionId(Long questionId) {
        if (this.question == null) {
            this.question = new Question();
        }
        this.question.setId(questionId);
    }
}
