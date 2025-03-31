package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.exception.DuplicateEnrollmentException;
import utc.englishlearning.Encybara.exception.NoSuitableCoursesException;

import java.util.List;

@Service
public class EnrollmentHelper {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRecommendationService courseRecommendationService;

    /**
     * Check if an enrollment already exists for the given user and course
     */
    public void checkDuplicateEnrollment(User user, Course course) {
        if (enrollmentRepository.existsByUserAndCourseAndProStatusTrue(user, course)) {
            throw new DuplicateEnrollmentException(
                    String.format("User %d already enrolled in course %d", user.getId(), course.getId()));
        }
    }

    /**
     * Creates recommendations with higher or equal difficulty level
     * Uses SERIALIZABLE isolation and retries on conflicts
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<Enrollment> createProgressiveRecommendations(User user, Learning_Result learningResult,
            double minLevel) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // Clean old recommendations first
                enrollmentRepository.deleteByUserAndProStatusFalse(user);

                return createRecommendationsInternal(user, learningResult, minLevel);
            } catch (DataIntegrityViolationException e) {
                attempt++;
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed to create recommendations after " + maxRetries + " attempts", e);
                }
                // Short sleep before retry
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying recommendation creation", ie);
                }
            }
        }

        throw new RuntimeException("Failed to create recommendations after exhausting retries");
    }

    /**
     * Internal method to create recommendations
     */
    private List<Enrollment> createRecommendationsInternal(User user, Learning_Result learningResult, double minLevel) {
        double currentMin = minLevel;
        double currentMax = minLevel + 0.5;
        List<Enrollment> createdEnrollments = new java.util.ArrayList<>();

        while (createdEnrollments.isEmpty() && currentMax <= 7.0) {
            try {
                List<Course> recommendedCourses = courseRecommendationService.getRecommendedCoursesWithRange(
                        learningResult, currentMin, currentMax);

                for (Course course : recommendedCourses) {
                    if (!course.getName().contains("(Placement)")) {
                        try {
                            Enrollment enrollment = createCourseEnrollment(user, course, learningResult, false);
                            createdEnrollments.add(enrollment);
                        } catch (DuplicateEnrollmentException e) {
                            // Ignore duplicates for recommendations
                            continue;
                        }
                    }
                }
            } catch (NoSuitableCoursesException e) {
                // No courses in current range, continue to next range
            }

            currentMax = Math.min(7.0, currentMax + 0.5);
        }

        if (createdEnrollments.isEmpty()) {
            throw new NoSuitableCoursesException(
                    "No suitable courses found even with expanded difficulty range");
        }

        return createdEnrollments;
    }

    /**
     * Safely create course enrollment after checking for duplicates
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Enrollment createCourseEnrollment(User user, Course course, Learning_Result learningResult,
            boolean proStatus) {
        if (proStatus) {
            checkDuplicateEnrollment(user, course);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setLearningResult(learningResult);
        enrollment.setEnrollDate(java.time.Instant.now());
        enrollment.setProStatus(proStatus);
        enrollment.setComLevel(0.0);
        enrollment.setTotalPoints(0);
        return enrollmentRepository.save(enrollment);
    }
}
