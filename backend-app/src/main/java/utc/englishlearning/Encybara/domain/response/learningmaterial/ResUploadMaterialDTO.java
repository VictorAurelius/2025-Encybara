package utc.englishlearning.Encybara.domain.response.learningmaterial;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResUploadMaterialDTO {
    private String materType;
    private String materLink;
    private Instant uploadedAt;
    private Long questionId;
    private Long lessonId;

    // Constructor with all fields
    public ResUploadMaterialDTO(String materLink, Instant uploadedAt, String materType, Long questionId,
            Long lessonId) {
        this.materLink = materLink;
        this.uploadedAt = uploadedAt;
        this.materType = materType;
        this.questionId = questionId;
        this.lessonId = lessonId;
    }
}
