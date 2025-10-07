package utc.englishlearning.Encybara.domain.response.scoring;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoringResponse {
    private double score; // Thang điểm 10
    private String improvements; // Gợi ý cải thiện từ advanced_answer
}