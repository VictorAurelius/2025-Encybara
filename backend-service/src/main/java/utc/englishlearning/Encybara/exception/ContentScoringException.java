package utc.englishlearning.Encybara.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ContentScoringException extends RuntimeException {
    private final int statusCode;

    public ContentScoringException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ContentScoringException(String message) {
        super(message);
        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public int getStatusCode() {
        return statusCode;
    }
}