package utc.englishlearning.Encybara.domain.request.speaking;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

@Getter
@Setter
public class ReqUpdateSpeakingSampleAnswerDTO {

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Answer content is required")
    @Size(min = 10, max = 5000, message = "Answer content must be between 10 and 5000 characters")
    private String answerContent;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Min(value = 1, message = "Difficulty level must be between 1 and 5")
    @Max(value = 5, message = "Difficulty level must be between 1 and 5")
    private Integer difficultyLevel;

    @DecimalMin(value = "0.0", message = "Estimated score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Estimated score must be between 0 and 100")
    private Double estimatedScore;
}