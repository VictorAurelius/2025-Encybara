package utc.englishlearning.Encybara.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardGroupResponse {
    private Long id; // ID của nhóm flashcard
    private String name; // Tên nhóm flashcard
    private Long userId; // ID của người dùng

    // Có thể thêm các trường khác nếu cần
}