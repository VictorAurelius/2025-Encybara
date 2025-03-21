package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.domain.Enrollment;
import utc.englishlearning.Encybara.domain.Learning_Result;
import utc.englishlearning.Encybara.repository.CourseRepository;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseRecommendationService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public List<Course> getRecommendedCourses(Learning_Result learningResult) {
        List<Course> recommendations = new ArrayList<>();

        // Get recommended courses for each skill
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.LISTENING));
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.SPEAKING));
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.READING));
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.WRITING));

        // Add ALLSKILLS courses if overall progress is good
        if (isOverallProgressGood(learningResult)) {
            recommendations.addAll(getRecommendedAllSkillsCourses(learningResult));
        }

        // Prioritize recommendations based on skill gaps
        return prioritizeRecommendations(recommendations, learningResult);
    }

    private List<Course> getRecommendedCoursesForSkill(Learning_Result learningResult, SkillTypeEnum skill) {
        double currentLevel = getSkillLevel(learningResult, skill);
        double[] difficultyRange = calculateRecommendedDifficultyRange(learningResult, skill);

        // Get courses within the difficulty range for the skill
        return courseRepository.findByCourseTypeAndDiffLevelBetween(
                mapSkillTypeToCourseType(skill),
                difficultyRange[0],
                difficultyRange[1]);
    }

    private double[] calculateRecommendedDifficultyRange(Learning_Result learningResult, SkillTypeEnum skill) {
        double currentLevel = getSkillLevel(learningResult, skill);
        double lowerBound = currentLevel - 0.5;
        double upperBound = currentLevel + 0.5;

        // Adjust range based on recent performance
        List<Enrollment> recentEnrollments = getRecentEnrollments(learningResult.getUser().getId(), skill, 5);

        if (!recentEnrollments.isEmpty()) {
            double avgCompletion = recentEnrollments.stream()
                    .mapToDouble(Enrollment::getComLevel)
                    .average()
                    .orElse(0.0);

            // Adjust difficulty based on completion rates
            if (areAllCompletionRatesLow(recentEnrollments)) {
                lowerBound -= 0.5;
                upperBound -= 0.5;
            } else if (areAllCompletionRatesHigh(recentEnrollments)) {
                lowerBound += 0.5;
                upperBound += 0.5;
            }

            // Check for stagnation or progress
            boolean isStagnating = isSkillStagnating(learningResult, skill);
            if (isStagnating) {
                lowerBound -= 0.5; // Suggest easier courses to build foundation
            }
        }

        // Ensure bounds stay within valid range (1.0 - 7.0)
        return new double[] {
                Math.max(1.0, lowerBound),
                Math.min(7.0, upperBound)
        };
    }

    private double getSkillLevel(Learning_Result learningResult, SkillTypeEnum skill) {
        return switch (skill) {
            case LISTENING -> learningResult.getListeningScore();
            case SPEAKING -> learningResult.getSpeakingScore();
            case READING -> learningResult.getReadingScore();
            case WRITING -> learningResult.getWritingScore();
            default -> throw new IllegalArgumentException("Invalid skill type");
        };
    }

    private boolean areAllCompletionRatesLow(List<Enrollment> enrollments) {
        return enrollments.stream()
                .allMatch(e -> e.getComLevel() < 50.0);
    }

    private boolean areAllCompletionRatesHigh(List<Enrollment> enrollments) {
        return enrollments.stream()
                .allMatch(e -> e.getComLevel() > 80.0);
    }

    private boolean isSkillStagnating(Learning_Result learningResult, SkillTypeEnum skill) {
        double currentScore = getSkillLevel(learningResult, skill);
        double previousScore = getPreviousSkillLevel(learningResult, skill);
        List<Enrollment> recentEnrollments = getRecentEnrollments(learningResult.getUser().getId(), skill, 3);

        return currentScore <= previousScore && !recentEnrollments.isEmpty();
    }

    private double getPreviousSkillLevel(Learning_Result learningResult, SkillTypeEnum skill) {
        return switch (skill) {
            case LISTENING -> learningResult.getPreviousListeningScore();
            case SPEAKING -> learningResult.getPreviousSpeakingScore();
            case READING -> learningResult.getPreviousReadingScore();
            case WRITING -> learningResult.getPreviousWritingScore();
            default -> throw new IllegalArgumentException("Invalid skill type");
        };
    }

    private List<Course> getRecommendedAllSkillsCourses(Learning_Result learningResult) {
        double averageLevel = (learningResult.getListeningScore() +
                learningResult.getSpeakingScore() +
                learningResult.getReadingScore() +
                learningResult.getWritingScore()) / 4.0;

        return courseRepository.findByCourseTypeAndDiffLevelBetween(
                CourseTypeEnum.ALLSKILLS,
                averageLevel - 0.5,
                averageLevel + 0.5);
    }

    private boolean isOverallProgressGood(Learning_Result learningResult) {
        // Check if all skills are improving
        return learningResult.getListeningScore() > learningResult.getPreviousListeningScore() &&
                learningResult.getSpeakingScore() > learningResult.getPreviousSpeakingScore() &&
                learningResult.getReadingScore() > learningResult.getPreviousReadingScore() &&
                learningResult.getWritingScore() > learningResult.getPreviousWritingScore();
    }

    private List<Course> prioritizeRecommendations(List<Course> recommendations, Learning_Result learningResult) {
        // Find the highest skill level
        double maxSkillLevel = Math.max(
                Math.max(learningResult.getListeningScore(), learningResult.getSpeakingScore()),
                Math.max(learningResult.getReadingScore(), learningResult.getWritingScore()));

        // Sort recommendations prioritizing courses for skills that need improvement
        return recommendations.stream()
                .sorted((c1, c2) -> {
                    double skill1Gap = getSkillGap(c1.getCourseType(), learningResult, maxSkillLevel);
                    double skill2Gap = getSkillGap(c2.getCourseType(), learningResult, maxSkillLevel);
                    return Double.compare(skill2Gap, skill1Gap); // Larger gaps first
                })
                .collect(Collectors.toList());
    }

    private double getSkillGap(CourseTypeEnum courseType, Learning_Result learningResult, double maxSkillLevel) {
        double skillLevel = switch (courseType) {
            case LISTENING -> learningResult.getListeningScore();
            case SPEAKING -> learningResult.getSpeakingScore();
            case READING -> learningResult.getReadingScore();
            case WRITING -> learningResult.getWritingScore();
            case ALLSKILLS -> (learningResult.getListeningScore() + learningResult.getSpeakingScore() +
                    learningResult.getReadingScore() + learningResult.getWritingScore()) / 4.0;
            default -> 0.0;
        };
        return maxSkillLevel - skillLevel;
    }

    private List<Enrollment> getRecentEnrollments(Long userId, SkillTypeEnum skill, int limit) {
        return enrollmentRepository.findByUserIdAndCourseTypeSortedByEnrollDate(
                userId,
                mapSkillTypeToCourseType(skill),
                PageRequest.of(0, limit));
    }

    private CourseTypeEnum mapSkillTypeToCourseType(SkillTypeEnum skillType) {
        return switch (skillType) {
            case LISTENING -> CourseTypeEnum.LISTENING;
            case SPEAKING -> CourseTypeEnum.SPEAKING;
            case READING -> CourseTypeEnum.READING;
            case WRITING -> CourseTypeEnum.WRITING;
            case ALLSKILLS -> CourseTypeEnum.ALLSKILLS;
        };
    }
}