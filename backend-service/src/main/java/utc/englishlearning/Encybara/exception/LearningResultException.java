package utc.englishlearning.Encybara.exception;

public class LearningResultException extends RuntimeException {
    public LearningResultException(String message) {
        super(message);
    }

    public static class LearningResultNotFoundException extends LearningResultException {
        public LearningResultNotFoundException(Long userId) {
            super("Learning result not found for user: " + userId);
        }
    }

    public static class InvalidSkillScoreException extends LearningResultException {
        public InvalidSkillScoreException(double score) {
            super("Invalid skill score: " + score + ". Score must be between 1.0 and 7.0");
        }
    }

    public static class InvalidSkillTypeException extends LearningResultException {
        public InvalidSkillTypeException(String skillType) {
            super("Invalid skill type: " + skillType);
        }
    }

    public static class InsufficientDataException extends LearningResultException {
        public InsufficientDataException() {
            super("Insufficient enrollment history data to calculate learning progress");
        }
    }
}