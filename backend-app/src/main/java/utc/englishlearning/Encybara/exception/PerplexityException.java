package utc.englishlearning.Encybara.exception;

public class PerplexityException extends RuntimeException {
    private final int statusCode;

    public PerplexityException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}