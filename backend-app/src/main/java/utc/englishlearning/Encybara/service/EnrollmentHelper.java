package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
     * 
     * @throws DuplicateEnrollmentException if duplicate enrollment is found
     */
    public void checkDuplicateEnrollment(User user, Course course) {
        if (enrollmentRepository.existsByUserAndCourseAndProStatusTrue(user, course)) {
            throw new DuplicateEnrollmentException(
                    String.format("User %d already enrolled in course %d", user.getId(), course.getId()));
        }
    }

    /**
     * Creates recommendations with higher or equal difficulty level, ensuring at
     * least one recommendation
     * 
     * @param minLevel Minimum difficulty level for recommendations
     */
    public List<Enrollment> createProgressiveRecommendations(User user, Learning_Result learningResult,
            double minLevel) {
        // Range will only expand upward from minLevel
        double currentMin = minLevel;
        double currentMax = minLevel + 0.5;
        List<Enrollment> createdEnrollments = new java.util.ArrayList<>();

        // Keep trying with increasing upper bound until we get at least one valid
        // course
        while (createdEnrollments.isEmpty() && currentMax <= 7.0) {
            try {
                List<Course> recommendedCourses = courseRecommendationService.getRecommendedCoursesWithRange(
                        learningResult, currentMin, currentMax);

                // Create enrollments for recommended courses
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
                // No courses found in current range
            }

            // Increase upper bound for next attempt
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
    public Enrollment createCourseEnrollment(User user, Course course, Learning_Result learningResult,
            boolean proStatus) {
        // Skip duplicate check for non-pro status (recommendations)
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