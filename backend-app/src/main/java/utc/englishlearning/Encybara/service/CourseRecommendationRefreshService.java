package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;
import utc.englishlearning.Encybara.exception.NoSuitableCoursesException;

import java.time.Instant;
import java.util.List;

@Service
public class CourseRecommendationRefreshService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

    @Transactional
    public void refreshAllRecommendations() {
        System.out.println(">>> STARTING COURSE RECOMMENDATIONS REFRESH");

        // Get all users
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                refreshUserRecommendations(user);
            } catch (Exception e) {
                System.err.println("Error refreshing recommendations for user " + user.getId() + ": " + e.getMessage());
            }
        }

        System.out.println(">>> FINISHED COURSE RECOMMENDATIONS REFRESH");
    }

    @Transactional
    private void refreshUserRecommendations(User user) {
        Learning_Result learningResult = user.getLearningResult();
        if (learningResult == null) {
            System.out.println("Skipping user " + user.getId() + " - no learning result found");
            return;
        }

        // Delete existing non-started recommendations
        enrollmentRepository.deleteByUserAndProStatusFalse(user);

        try {
            // Try primary recommendation strategy
            List<Course> recommendedCourses = courseRecommendationService.getRecommendedCourses(learningResult);
            int created = createEnrollments(user, learningResult, recommendedCourses);
            if (created == 0) {
                throw new NoSuitableCoursesException("No suitable courses found after primary recommendation");
            }
        } catch (NoSuitableCoursesException e) {
            // Fallback: Try strongest skill matching
            double listeningLevel = learningResult.getListeningScore();
            double speakingLevel = learningResult.getSpeakingScore();
            double readingLevel = learningResult.getReadingScore();
            double writingLevel = learningResult.getWritingScore();

            // Start with ALLSKILLS as default type and average score as baseline
            CourseTypeEnum recommendedType = CourseTypeEnum.ALLSKILLS;
            double highestScore = (listeningLevel + speakingLevel + readingLevel + writingLevel) / 4.0;
            double baseScore = highestScore; // Keep track of base score for final fallback

            // Find the strongest skill
            if (listeningLevel > highestScore) {
                highestScore = listeningLevel;
                recommendedType = CourseTypeEnum.LISTENING;
            }
            if (speakingLevel > highestScore) {
                highestScore = speakingLevel;
                recommendedType = CourseTypeEnum.SPEAKING;
            }
            if (readingLevel > highestScore) {
                highestScore = readingLevel;
                recommendedType = CourseTypeEnum.READING;
            }
            if (writingLevel > highestScore) {
                highestScore = writingLevel;
                recommendedType = CourseTypeEnum.WRITING;
            }

            try {
                List<Course> fallbackCourses = courseRecommendationService.getRecommendedCoursesWithRange(
                        learningResult,
                        highestScore - 0.5,
                        highestScore + 0.5);

                if (!fallbackCourses.isEmpty()) {
                    int created = createEnrollments(user, learningResult, fallbackCourses);
                    if (created > 0) {
                        return;
                    }
                }
            } catch (Exception ignored) {
                // Continue to next fallback
            }

            // Ultimate fallback: Find most improved skill
            double listeningImprovement = listeningLevel - learningResult.getPreviousListeningScore();
            double speakingImprovement = speakingLevel - learningResult.getPreviousSpeakingScore();
            double readingImprovement = readingLevel - learningResult.getPreviousReadingScore();
            double writingImprovement = writingLevel - learningResult.getPreviousWritingScore();

            CourseTypeEnum improvedType = CourseTypeEnum.ALLSKILLS;
            double bestImprovement = 0;
            double skillLevel = highestScore;

            if (listeningImprovement > bestImprovement) {
                bestImprovement = listeningImprovement;
                improvedType = CourseTypeEnum.LISTENING;
                skillLevel = listeningLevel;
            }
            if (speakingImprovement > bestImprovement) {
                bestImprovement = speakingImprovement;
                improvedType = CourseTypeEnum.SPEAKING;
                skillLevel = speakingLevel;
            }
            if (readingImprovement > bestImprovement) {
                bestImprovement = readingImprovement;
                improvedType = CourseTypeEnum.READING;
                skillLevel = readingLevel;
            }
            if (writingImprovement > bestImprovement) {
                bestImprovement = writingImprovement;
                improvedType = CourseTypeEnum.WRITING;
                skillLevel = writingLevel;
            }

            try {
                List<Course> improvedSkillCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                        improvedType,
                        skillLevel - 0.5,
                        skillLevel + 0.5,
                        CourseStatusEnum.PUBLIC);

                if (!improvedSkillCourses.isEmpty()) {
                    int created = createEnrollments(user, learningResult, improvedSkillCourses);
                    if (created > 0) {
                        return;
                    }
                }
            } catch (Exception ignored) {
                // Continue to final fallback
            }

            // Try multiple fallback strategies with increasing ranges until we find
            // something
            double baseLevel = (listeningLevel + speakingLevel + readingLevel + writingLevel) / 4.0;
            List<Course> finalCourses = null;

            // Try 1: ALLSKILLS at exact level
            finalCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                    CourseTypeEnum.ALLSKILLS,
                    Math.max(1.0, baseLevel - 0.5),
                    Math.min(7.0, baseLevel + 0.5),
                    CourseStatusEnum.PUBLIC);

            if (finalCourses.isEmpty()) {
                // Try 2: Any type at exact level
                for (CourseTypeEnum type : CourseTypeEnum.values()) {
                    finalCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                            type,
                            Math.max(1.0, baseLevel - 0.5),
                            Math.min(7.0, baseLevel + 0.5),
                            CourseStatusEnum.PUBLIC);
                    if (!finalCourses.isEmpty())
                        break;
                }
            }

            if (finalCourses.isEmpty()) {
                // Try 3: ALLSKILLS with wider range
                finalCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                        CourseTypeEnum.ALLSKILLS,
                        1.0, // Start from beginner level
                        Math.min(7.0, baseLevel + 1.0), // Up to slightly above current level
                        CourseStatusEnum.PUBLIC);
            }

            if (finalCourses.isEmpty()) {
                // Final attempt: Get any available course
                finalCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                        CourseTypeEnum.ALLSKILLS,
                        1.0,
                        7.0,
                        CourseStatusEnum.PUBLIC);
            }

            // At this point, if we still have no courses, create a dummy enrollment with
            // the first available course
            if (finalCourses.isEmpty()) {
                Course fallbackCourse = courseRepository.findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No courses available in the system"));
                finalCourses = List.of(fallbackCourse);
            }

            int created = createEnrollments(user, learningResult, finalCourses);

            if (created == 0) {
                System.out.println("Warning: Unable to create any recommendations for user " + user.getId() +
                        " despite multiple fallback attempts");
            }
        }
    }

    private int createEnrollments(User user, Learning_Result learningResult, List<Course> courses) {
        int created = 0;
        for (Course course : courses) {
            if (hasActiveEnrollment(user, course)) {
                continue;
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollment.setEnrollDate(Instant.now());
            enrollment.setProStatus(false);
            enrollment.setComLevel(0.0);
            enrollment.setTotalPoints(0);
            enrollment.setLearningResult(learningResult);

            enrollmentRepository.save(enrollment);
            created++;
        }
        System.out.println("Refreshed recommendations for user " + user.getId() +
                " - Created " + created + " new recommendations");
        return created;
    }

    private boolean hasActiveEnrollment(User user, Course course) {
        return enrollmentRepository.existsByUserAndCourseAndProStatusTrue(user, course);
    }
}