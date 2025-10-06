package utc.englishlearning.Encybara.service.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecommendationRange {
    private double baseLevel;
    private double currentRange;
    private static final double INITIAL_RANGE = 0.5;
    private static final double RANGE_INCREMENT = 0.5;
    private static final double MAX_RANGE = 2.0;

    public RecommendationRange(double baseLevel) {
        this.baseLevel = baseLevel;
        this.currentRange = INITIAL_RANGE;
    }

    public boolean increaseRange() {
        if (currentRange < MAX_RANGE) {
            currentRange += RANGE_INCREMENT;
            return true;
        }
        return false;
    }

    public double getLowerBound() {
        return Math.max(1.0, baseLevel - currentRange);
    }

    public double getUpperBound() {
        return Math.min(7.0, baseLevel + currentRange);
    }
}