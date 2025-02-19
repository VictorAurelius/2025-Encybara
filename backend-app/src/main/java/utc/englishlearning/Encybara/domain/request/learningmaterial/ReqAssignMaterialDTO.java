package utc.englishlearning.Encybara.domain.request.learningmaterial;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAssignMaterialDTO {
    private String materLink; // Liên kết tài liệu
    private Long questionId; // ID của câu hỏi
    private Long lessonId; // ID của bài học
    private String materType; // Add this line

    public String getMaterType() { // Add this method
        return materType;
    }

    public void setMaterType(String materType) { // Add this method
        this.materType = materType;
    }
}