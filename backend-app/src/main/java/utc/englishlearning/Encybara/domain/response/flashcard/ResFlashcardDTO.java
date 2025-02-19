package utc.englishlearning.Encybara.domain.response.flashcard;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResFlashcardDTO {
    private long id;
    private String word;
    private String definitions;
    private String VietNameseMeaning;
    private String partOfSpeech;
    private String phoneticText;
    private String phoneticAudio;
    private Instant addedDate;
    private boolean learnedStatus;
    private Instant lastReviewed;
    private String examples;
    private Long userId;

    public Instant getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(Instant lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public String getPhoneticText() {
        return phoneticText;
    }

    public void setPhoneticText(String phoneticText) {
        this.phoneticText = phoneticText;
    }

    public String getPhoneticAudio() {
        return phoneticAudio;
    }

    public void setPhoneticAudio(String phoneticAudio) {
        this.phoneticAudio = phoneticAudio;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}