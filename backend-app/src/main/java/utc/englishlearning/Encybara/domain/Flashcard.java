package utc.englishlearning.Encybara.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flashcards")
@Getter
@Setter
public class Flashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String word;
    private boolean learnedStatus;
    private Instant addedDate;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String examples;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String definitions;
    private String vietNameseMeaning;
    private Instant lastReviewed;
    private String partOfSpeech;
    private String phoneticText;
    private String phoneticAudio;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "flashcard_group_id")
    private FlashcardGroup flashcardGroup;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    public String getVietNameseMeaning() {
        return vietNameseMeaning;
    }

    public void setVietNameseMeaning(String vietNameseMeaning) {
        this.vietNameseMeaning = vietNameseMeaning;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }
}
