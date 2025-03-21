package utc.englishlearning.Encybara.util.constant;

public enum EnglishLevelEnum {
    BEGINNER(1.0, 2.0, "Beginner"),
    ELEMENTARY(2.0, 3.0, "Elementary"),
    PRE_INTERMEDIATE(3.0, 4.0, "Pre-Intermediate"),
    INTERMEDIATE(4.0, 5.0, "Intermediate"),
    UPPER_INTERMEDIATE(5.0, 6.0, "Upper Intermediate"),
    ADVANCED(6.0, 7.0, "Advanced");

    private final double minScore;
    private final double maxScore;
    private final String displayName;

    EnglishLevelEnum(double minScore, double maxScore, String displayName) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.displayName = displayName;
    }

    public static EnglishLevelEnum fromScore(double score) {
        for (EnglishLevelEnum level : values()) {
            if (score >= level.minScore && score < level.maxScore) {
                return level;
            }
        }
        // Default to BEGINNER if score is too low, ADVANCED if too high
        return score < BEGINNER.minScore ? BEGINNER : ADVANCED;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }
}