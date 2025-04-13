package utc.englishlearning.Encybara.domain.response.speechToText;

public class SpeechResponseDTO {
    private SpeechData data;
    private String status;
    private String message;

    public SpeechResponseDTO(SpeechData data, String status, String message) {
        this.data = data;
        this.status = status;
        this.message = message;
    }

    // Getters và Setters
    public SpeechData getData() {
        return data;
    }

    public void setData(SpeechData data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

