package utc.englishlearning.Encybara.domain.request.enrollment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateEnrollmentDTO {
    private Long userId;
    private Long courseId;
}
