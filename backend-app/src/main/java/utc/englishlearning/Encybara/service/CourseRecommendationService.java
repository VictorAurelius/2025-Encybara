package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.domain.Learning_Result;
import utc.englishlearning.Encybara.repository.CourseRepository;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseRecommendationService {

    private static final double MAX_RECOMMENDED_LEVEL_INCREASE = 1.0;
    private static final double MIN_COMPLETION_RATE_FOR_HIGHER_LEVEL = 80.0;
    private static final double MAX_SKILL_GAP_FOR_ALLSKILLS = 1.0;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public List<Course> getRecommendedCourses(Learning_Result learningResult) {
        List<Course> recommendations = new ArrayList<>();

        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.LISTENING));
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.SPEAKING));
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.READING));
        recommendations.addAll(getRecommendedCoursesForSkill(learningResult, SkillTypeEnum.WRITING));

        if (isReadyForAllSkills(learningResult)) {
            recommendations.addAll(getRecommendedAllSkillsCourses(learningResult));
        }

        return filterAndPrioritizeRecommendations(recommendations, learningResult);
    }

    private boolean isReadyForAllSkills(Learning_Result learningResult) {
        double maxSkill = Math.max(
                Math.max(learningResult.getListeningScore(), learningResult.getSpeakingScore()),
                Math.max(learningResult.getReadingScore(), learningResult.getWritingScore()));
        double minSkill = Math.min(
                Math.min(learningResult.getListeningScore(), learningResult.getSpeakingScore()),
                Math.min(learningResult.getReadingScore(), learningResult.getWritingScore()));

        return (maxSkill - minSkill <= MAX_SKILL_GAP_FOR_ALLSKILLS) && isOverallProgressGood(learningResult);
    }

    private List<Course> getRecommendedCoursesForSkill(Learning_Result learningResult, SkillTypeEnum skill) {
        if (skill == SkillTypeEnum.ALLSKILLS) {
            return getRecommendedAllSkillsCourses(learningResult);
        }

        double currentLevel = getSkillLevel(learningResult, skill);
        double[] difficultyRange = calculateRecommendedDifficultyRange(learningResult, skill);

        return courseRepository.findPublicCoursesByTypeAndLevelRange(
                mapSkillTypeToCourseType(skill),
                difficultyRange[0],
                difficultyRange[1],
                CourseStatusEnum.PUBLIC);
    }

    private boolean isAppropriateLevel(Course course, double currentLevel) {
        return course.getRecomLevel() <= currentLevel &&
                course.getDiffLevel() <= currentLevel + MAX_RECOMMENDED_LEVEL_INCREASE;
    }

    private double[] calculateRecommendedDifficultyRange(Learning_Result learningResult, SkillTypeEnum skill) {
        double currentLevel = getSkillLevel(learningResult, skill);
        double lowerBound = currentLevel - 0.5;
        double upperBound = currentLevel + 0.5;

        Double avgCompletionRate = enrollmentRepository.getAverageCompletionRateLastMonth(
                learningResult.getUser().getId(),
                mapSkillTypeToCourseType(skill).name());

        if (avgCompletionRate != null) {
            if (avgCompletionRate >= MIN_COMPLETION_RATE_FOR_HIGHER_LEVEL) {
                upperBound = Math.min(7.0, currentLevel + MAX_RECOMMENDED_LEVEL_INCREASE);
            } else if (avgCompletionRate < 50.0) {
                lowerBound = Math.max(1.0, currentLevel - 1.0);
                upperBound = currentLevel;
            }

            if (isSkillStagnating(learningResult, skill)) {
                upperBound = currentLevel;
                lowerBound = Math.max(1.0, currentLevel - 0.5);
            }
        }

        if (skill == SkillTypeEnum.ALLSKILLS) {
            lowerBound = Math.max(lowerBound, currentLevel - 0.5);
            upperBound = Math.min(upperBound, currentLevel + 0.5);
        }

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
            case ALLSKILLS -> {
                double avg = (learningResult.getListeningScore() +
                        learningResult.getSpeakingScore() +
                        learningResult.getReadingScore() +
                        learningResult.getWritingScore()) / 4.0;
                yield Math.round(avg * 2) / 2.0;
            }
        };
    }

    private boolean isSkillStagnating(Learning_Result learningResult, SkillTypeEnum skill) {
        double currentScore = getSkillLevel(learningResult, skill);
        double previousScore = getPreviousSkillLevel(learningResult, skill);

        Double avgCompletionRate = enrollmentRepository.getAverageCompletionRateLastMonth(
                learningResult.getUser().getId(),
                mapSkillTypeToCourseType(skill).name());

        return currentScore <= previousScore || (avgCompletionRate != null && avgCompletionRate < 60.0);
    }

    private double getPreviousSkillLevel(Learning_Result learningResult, SkillTypeEnum skill) {
        return switch (skill) {
            case LISTENING -> learningResult.getPreviousListeningScore();
            case SPEAKING -> learningResult.getPreviousSpeakingScore();
            case READING -> learningResult.getPreviousReadingScore();
            case WRITING -> learningResult.getPreviousWritingScore();
            case ALLSKILLS -> {
                double avg = (learningResult.getPreviousListeningScore() +
                        learningResult.getPreviousSpeakingScore() +
                        learningResult.getPreviousReadingScore() +
                        learningResult.getPreviousWritingScore()) / 4.0;
                yield Math.round(avg * 2) / 2.0;
            }
        };
    }

    private List<Course> getRecommendedAllSkillsCourses(Learning_Result learningResult) {
        double averageLevel = getSkillLevel(learningResult, SkillTypeEnum.ALLSKILLS);
        double[] difficultyRange = calculateRecommendedDifficultyRange(learningResult, SkillTypeEnum.ALLSKILLS);

        return courseRepository.findPublicCoursesByTypeAndLevelRange(
                CourseTypeEnum.ALLSKILLS,
                difficultyRange[0],
                difficultyRange[1],
                CourseStatusEnum.PUBLIC);
    }

    private boolean isOverallProgressGood(Learning_Result learningResult) {
        boolean skillsImproving = learningResult.getListeningScore() > learningResult.getPreviousListeningScore() &&
                learningResult.getSpeakingScore() > learningResult.getPreviousSpeakingScore() &&
                learningResult.getReadingScore() > learningResult.getPreviousReadingScore() &&
                learningResult.getWritingScore() > learningResult.getPreviousWritingScore();

        Double listeningCompletion = enrollmentRepository.getAverageCompletionRateLastMonth(
                learningResult.getUser().getId(), CourseTypeEnum.LISTENING.name());
        Double speakingCompletion = enrollmentRepository.getAverageCompletionRateLastMonth(
                learningResult.getUser().getId(), CourseTypeEnum.SPEAKING.name());
        Double readingCompletion = enrollmentRepository.getAverageCompletionRateLastMonth(
                learningResult.getUser().getId(), CourseTypeEnum.READING.name());
        Double writingCompletion = enrollmentRepository.getAverageCompletionRateLastMonth(
                learningResult.getUser().getId(), CourseTypeEnum.WRITING.name());

        boolean goodCompletionRates = (listeningCompletion == null
                || listeningCompletion >= MIN_COMPLETION_RATE_FOR_HIGHER_LEVEL) &&
                (speakingCompletion == null || speakingCompletion >= MIN_COMPLETION_RATE_FOR_HIGHER_LEVEL) &&
                (readingCompletion == null || readingCompletion >= MIN_COMPLETION_RATE_FOR_HIGHER_LEVEL) &&
                (writingCompletion == null || writingCompletion >= MIN_COMPLETION_RATE_FOR_HIGHER_LEVEL);

        return skillsImproving && goodCompletionRates;
    }

    private List<Course> filterAndPrioritizeRecommendations(List<Course> recommendations,
            Learning_Result learningResult) {
        double maxSkillLevel = Math.max(
                Math.max(learningResult.getListeningScore(), learningResult.getSpeakingScore()),
                Math.max(learningResult.getReadingScore(), learningResult.getWritingScore()));

        return recommendations.stream()
                .filter(course -> isAppropriateLevel(course, getSkillLevel(learningResult,
                        mapCourseTypeToSkillType(course.getCourseType()))))
                .sorted((c1, c2) -> {
                    double skill1Gap = getSkillGap(c1.getCourseType(), learningResult, maxSkillLevel);
                    double skill2Gap = getSkillGap(c2.getCourseType(), learningResult, maxSkillLevel);

                    int gapCompare = Double.compare(skill2Gap, skill1Gap);
                    if (gapCompare != 0) {
                        return gapCompare;
                    }

                    if (c1.getCourseType() == CourseTypeEnum.ALLSKILLS
                            && c2.getCourseType() != CourseTypeEnum.ALLSKILLS) {
                        return isReadyForAllSkills(learningResult) ? -1 : 1;
                    }
                    if (c2.getCourseType() == CourseTypeEnum.ALLSKILLS
                            && c1.getCourseType() != CourseTypeEnum.ALLSKILLS) {
                        return isReadyForAllSkills(learningResult) ? 1 : -1;
                    }

                    return Double.compare(c1.getDiffLevel(), c2.getDiffLevel());
                })
                .collect(Collectors.toList());
    }

    private double getSkillGap(CourseTypeEnum courseType, Learning_Result learningResult, double maxSkillLevel) {
        double skillLevel = switch (courseType) {
            case LISTENING -> learningResult.getListeningScore();
            case SPEAKING -> learningResult.getSpeakingScore();
            case READING -> learningResult.getReadingScore();
            case WRITING -> learningResult.getWritingScore();
            case ALLSKILLS -> getSkillLevel(learningResult, SkillTypeEnum.ALLSKILLS);
        };
        return maxSkillLevel - skillLevel;
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

    private SkillTypeEnum mapCourseTypeToSkillType(CourseTypeEnum courseType) {
        return switch (courseType) {
            case LISTENING -> SkillTypeEnum.LISTENING;
            case SPEAKING -> SkillTypeEnum.SPEAKING;
            case READING -> SkillTypeEnum.READING;
            case WRITING -> SkillTypeEnum.WRITING;
            case ALLSKILLS -> SkillTypeEnum.ALLSKILLS;
        };
    }
}