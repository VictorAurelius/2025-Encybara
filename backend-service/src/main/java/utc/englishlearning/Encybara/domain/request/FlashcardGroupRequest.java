package utc.englishlearning.Encybara.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardGroupRequest {
    private String name; // Tên nhóm flashcard
    private Long userId; // ID của người dùng
}