package utc.englishlearning.Encybara.domain.response.discussion;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResDiscussionDTO {
    private Long id;
    private Long userId;
    private Long lessonId;
    private String content;
    private int numLike;
    private List<ResDiscussionDTO> replies; // Thảo luận con
}