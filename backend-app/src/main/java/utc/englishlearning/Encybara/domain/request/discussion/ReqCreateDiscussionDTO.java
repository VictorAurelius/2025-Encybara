package utc.englishlearning.Encybara.domain.request.discussion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateDiscussionDTO {
    private Long userId;
    private Long lessonId;
    private String content;
    private Long parentId; // Để trả lời thảo luận
}