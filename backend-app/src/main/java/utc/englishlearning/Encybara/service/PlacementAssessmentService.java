package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;

@Service
public class PlacementAssessmentService {

    @Autowired
    private LessonResultRepository lessonResultRepository;

    /**
     * Calculate user's level based on placement test completion percentage
     * Score ranges:
     * 0-20% -> 1.0
     * 21-35% -> 1.5
     * 36-50% -> 2.0
     * 51-65% -> 2.5
     * 66-75% -> 3.0
     * 76-80% -> 3.5
     * 81-85% -> 4.0
     * 86-90% -> 4.5
     * 91-100% -> 5.0
     */
    public double calculateLevelFromPlacement(Enrollment placementEnrollment) {
        if (placementEnrollment == null) {
            return 1.0; // Default level for no assessment
        }

        // Get all lesson results for this enrollment
        var lessonResults = lessonResultRepository.findByEnrollment(placementEnrollment);

        if (lessonResults.isEmpty()) {
            return 1.0;
        }

        // Calculate total points possible and achieved
        int totalPointsPossible = 0;
        int totalPointsAchieved = 0;

        for (Lesson_Result result : lessonResults) {
            Lesson lesson = result.getLesson();
            // Each question typically worth 1 point in placement test
            totalPointsPossible += lesson.getSumQues();
            totalPointsAchieved += result.getTotalPoints();
        }

        // Calculate completion percentage
        double completionPercentage = totalPointsPossible > 0
                ? (double) totalPointsAchieved / totalPointsPossible * 100
                : 0;

        // Map completion percentage to level score
        if (completionPercentage <= 20)
            return 1.0;
        if (completionPercentage <= 35)
            return 1.5;
        if (completionPercentage <= 50)
            return 2.0;
        if (completionPercentage <= 65)
            return 2.5;
        if (completionPercentage <= 75)
            return 3.0;
        if (completionPercentage <= 80)
            return 3.5;
        if (completionPercentage <= 85)
            return 4.0;
        if (completionPercentage <= 90)
            return 4.5;
        return 5.0;
    }

    /**
     * Calculate skill-specific scores based on lesson types
     */
    public SkillScores calculateSkillScores(Enrollment placementEnrollment) {
        SkillScores scores = new SkillScores();
        if (placementEnrollment == null) {
            return scores;
        }

        var lessonResults = lessonResultRepository.findByEnrollment(placementEnrollment);

        // Group results by skill type and calculate percentages
        for (Lesson_Result result : lessonResults) {
            Lesson lesson = result.getLesson();
            int possible = lesson.getSumQues();
            int achieved = result.getTotalPoints();
            double percentage = possible > 0 ? (double) achieved / possible * 100 : 0;

            switch (lesson.getSkillType()) {
                case LISTENING -> scores.listeningPercentage += percentage;
                case SPEAKING -> scores.speakingPercentage += percentage;
                case READING -> scores.readingPercentage += percentage;
                case WRITING -> scores.writingPercentage += percentage;
                case ALLSKILLS -> {
                    scores.listeningPercentage += percentage / 4;
                    scores.speakingPercentage += percentage / 4;
                    scores.readingPercentage += percentage / 4;
                    scores.writingPercentage += percentage / 4;
                }
            }
        }

        // Convert percentages to level scores
        scores.listeningScore = mapPercentageToScore(scores.listeningPercentage);
        scores.speakingScore = mapPercentageToScore(scores.speakingPercentage);
        scores.readingScore = mapPercentageToScore(scores.readingPercentage);
        scores.writingScore = mapPercentageToScore(scores.writingPercentage);

        return scores;
    }

    public SkillScores calculateSkillScoresFromTotalPoints(
            int listeningPoints,
            int speakingPoints,
            int readingPoints,
            int writingPoints) {
        SkillScores scores = new SkillScores();

        // Convert total points to percentages (assuming max 100 points per skill in
        // placement)
        scores.listeningPercentage = calculatePercentage(listeningPoints, 100);
        scores.speakingPercentage = calculatePercentage(speakingPoints, 100);
        scores.readingPercentage = calculatePercentage(readingPoints, 100);
        scores.writingPercentage = calculatePercentage(writingPoints, 100);

        // Convert percentages to level scores
        scores.listeningScore = mapPercentageToScore(scores.listeningPercentage);
        scores.speakingScore = mapPercentageToScore(scores.speakingPercentage);
        scores.readingScore = mapPercentageToScore(scores.readingPercentage);
        scores.writingScore = mapPercentageToScore(scores.writingPercentage);

        return scores;
    }

    private double calculatePercentage(int achieved, int possible) {
        return possible > 0 ? (double) achieved / possible * 100 : 0;
    }

    private double mapPercentageToScore(double percentage) {
        if (percentage <= 20)
            return 1.0;
        if (percentage <= 35)
            return 1.5;
        if (percentage <= 50)
            return 2.0;
        if (percentage <= 65)
            return 2.5;
        if (percentage <= 75)
            return 3.0;
        if (percentage <= 80)
            return 3.5;
        if (percentage <= 85)
            return 4.0;
        if (percentage <= 90)
            return 4.5;
        return 5.0;
    }

    public static class SkillScores {
        private double listeningPercentage = 0;
        private double speakingPercentage = 0;
        private double readingPercentage = 0;
        private double writingPercentage = 0;

        private double listeningScore = 1.0;
        private double speakingScore = 1.0;
        private double readingScore = 1.0;
        private double writingScore = 1.0;

        public double getListeningScore() {
            return listeningScore;
        }

        public double getSpeakingScore() {
            return speakingScore;
        }

        public double getReadingScore() {
            return readingScore;
        }

        public double getWritingScore() {
            return writingScore;
        }
    }
}