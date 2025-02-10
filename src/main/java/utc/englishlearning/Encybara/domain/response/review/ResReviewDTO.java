package utc.englishlearning.Encybara.domain.response.review;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.ReviewStatusEnum;

@Getter
@Setter
public class ResReviewDTO {
    private Long id;
    private Long userId;
    private Long courseId;
    private String reContent;
    private String reSubject;
    private int numStar;
    private int numLike;
    private ReviewStatusEnum status;
}