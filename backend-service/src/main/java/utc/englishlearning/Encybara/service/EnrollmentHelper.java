package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.exception.DuplicateEnrollmentException;
import utc.englishlearning.Encybara.exception.NoSuitableCoursesException;

import java.util.*;

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
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Enrollment> createProgressiveRecommendations(User user, Learning_Result learningResult,
            double minLevel) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // Delete within transaction but with new isolation level
                deleteExistingRecommendations(user);

                List<Enrollment> recommendations = createRecommendationsInternal(user, learningResult, minLevel);
                if (!recommendations.isEmpty()) {
                    return recommendations;
                }

                // If no recommendations were created, try next attempt
                attempt++;
                if (attempt < maxRetries) {
                    Thread.sleep(100); // Short delay between attempts
                }
            } catch (DataIntegrityViolationException e) {
                attempt++;
                if (attempt == maxRetries) {
                    // On final attempt, try to get any existing recommendations
                    List<Enrollment> existingRecommendations = enrollmentRepository
                            .findByUserIdAndProStatus(user.getId(), false, PageRequest.of(0, 5))
                            .getContent();

                    if (!existingRecommendations.isEmpty()) {
                        return existingRecommendations;
                    }
                    throw new RuntimeException(
                            "Failed to create or retrieve recommendations after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(100 * attempt); // Increasing delay between retries
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while retrying recommendation creation", ie);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while creating recommendations", e);
            }
        }

        throw new RuntimeException("Failed to create recommendations after exhausting retries");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void deleteExistingRecommendations(User user) {
        enrollmentRepository.deleteByUserAndProStatusFalse(user);
    }

    /**
     * Internal method to create recommendations
     */
    private List<Enrollment> createRecommendationsInternal(User user, Learning_Result learningResult, double minLevel) {
        List<Enrollment> createdEnrollments = new java.util.ArrayList<>();
        Set<Long> processedCourseIds = new HashSet<>();
        double baseLevel = minLevel;

        // Try progressive ranges up to 7.0
        while (createdEnrollments.isEmpty() && baseLevel <= 7.0) {
            double currentMin = Math.max(1.0, baseLevel - 0.5);
            double currentMax = Math.min(7.0, baseLevel + 0.5);

            try {
                // Get recommendations for current range
                List<Course> recommendedCourses = courseRecommendationService.getRecommendedCoursesWithRange(
                        learningResult, currentMin, currentMax);

                // Process each recommended course
                for (Course course : recommendedCourses) {
                    if (course.getName().contains("(Placement)") || processedCourseIds.contains(course.getId())) {
                        continue;
                    }

                    try {
                        Enrollment enrollment = createCourseEnrollment(user, course, learningResult, false);
                        createdEnrollments.add(enrollment);
                        processedCourseIds.add(course.getId());

                        // Stop after getting enough recommendations
                        if (createdEnrollments.size() >= 3) {
                            return createdEnrollments;
                        }
                    } catch (DuplicateEnrollmentException e) {
                        // Just skip duplicates
                        processedCourseIds.add(course.getId());
                    } catch (Exception e) {
                        // Log but continue with other courses
                        System.err.println(
                                "Failed to create enrollment for course " + course.getId() + ": " + e.getMessage());
                    }
                }
            } catch (NoSuitableCoursesException e) {
                // Continue to next range
            } catch (Exception e) {
                System.err.println("Error getting recommendations for range " + currentMin + "-" + currentMax + ": "
                        + e.getMessage());
            }

            // Increase base level for next iteration
            baseLevel += 0.5;
        }

        // If no recommendations created, try one last time with widest range
        if (createdEnrollments.isEmpty()) {
            try {
                List<Course> lastAttemptCourses = courseRecommendationService.getRecommendedCoursesWithRange(
                        learningResult, 1.0, 7.0);

                for (Course course : lastAttemptCourses) {
                    if (!processedCourseIds.contains(course.getId())) {
                        try {
                            Enrollment enrollment = createCourseEnrollment(user, course, learningResult, false);
                            createdEnrollments.add(enrollment);
                            break; // Get at least one recommendation
                        } catch (Exception ignored) {
                            // Keep trying other courses
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Final attempt to create recommendations failed: " + e.getMessage());
            }
        }

        // If still empty, throw exception for the fallback mechanism to handle
        if (createdEnrollments.isEmpty()) {
            throw new NoSuitableCoursesException("No suitable courses found after trying all ranges");
        }

        return createdEnrollments;
    }

    /**
     * Safely create course enrollment after checking for duplicates
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Enrollment createCourseEnrollment(User user, Course course, Learning_Result learningResult,
            boolean proStatus) {
        try {
            // Check for existing enrollment using proper repository method
            Optional<Enrollment> existingEnrollment = enrollmentRepository.findByUserIdAndCourseId(
                    user.getId(), course.getId());

            if (existingEnrollment.isPresent()) {
                Enrollment enrollment = existingEnrollment.get();
                // For pro enrollments, throw if already enrolled
                if (proStatus && enrollment.isProStatus()) {
                    throw new DuplicateEnrollmentException(
                            String.format("User %d already enrolled in course %d", user.getId(), course.getId()));
                }
                // For recommendations, update existing
                if (!proStatus) {
                    enrollment.setEnrollDate(java.time.Instant.now());
                    enrollment.setLearningResult(learningResult);
                    return enrollmentRepository.save(enrollment);
                }
            }

            // Create new enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollment.setLearningResult(learningResult);
            enrollment.setEnrollDate(java.time.Instant.now());
            enrollment.setProStatus(proStatus);
            enrollment.setComLevel(0.0);
            enrollment.setTotalPoints(0);

            try {
                return enrollmentRepository.save(enrollment);
            } catch (DataIntegrityViolationException e) {
                // Retry once on race condition
                existingEnrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId());
                if (existingEnrollment.isPresent()) {
                    if (proStatus) {
                        throw new DuplicateEnrollmentException(
                                String.format("User %d already enrolled in course %d", user.getId(), course.getId()));
                    }
                    // Update existing recommendation
                    Enrollment existing = existingEnrollment.get();
                    existing.setEnrollDate(java.time.Instant.now());
                    existing.setLearningResult(learningResult);
                    return enrollmentRepository.save(existing);
                }
                throw new RuntimeException("Failed to create enrollment after retry", e);
            }
        } catch (Exception e) {
            if (e instanceof DuplicateEnrollmentException) {
                throw e;
            }
            throw new RuntimeException("Failed to create enrollment", e);
        }
    }
}
