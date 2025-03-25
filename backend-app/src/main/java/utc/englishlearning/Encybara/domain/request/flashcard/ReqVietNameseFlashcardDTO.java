package utc.englishlearning.Encybara.domain.request.flashcard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqVietNameseFlashcardDTO {
    private String word; // The word to create a flashcard for
    private Long userId; // The ID of the user creating the flashcard
    private int meaningIndex; // The index of the meaning to use
}