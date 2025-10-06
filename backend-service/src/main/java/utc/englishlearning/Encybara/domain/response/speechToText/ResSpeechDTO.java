package utc.englishlearning.Encybara.domain.response.speechToText;

public class ResSpeechDTO {
    private String transcript;
    private Double confidence;

    public ResSpeechDTO() {}

    public ResSpeechDTO(String transcript, Double confidence) {
        this.transcript = transcript;
        this.confidence = confidence;
    }

    // Getters và Setters
    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
