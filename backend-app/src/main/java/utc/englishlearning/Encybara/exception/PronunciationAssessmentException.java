package utc.englishlearning.Encybara.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PronunciationAssessmentException extends RuntimeException {
    private final int statusCode;

    public PronunciationAssessmentException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public PronunciationAssessmentException(String message) {
        super(message);
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public int getStatusCode() {
        return statusCode;
    }
}