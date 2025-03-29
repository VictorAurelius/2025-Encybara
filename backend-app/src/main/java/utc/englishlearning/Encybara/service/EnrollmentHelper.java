package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.exception.DuplicateEnrollmentException;
import utc.englishlearning.Encybara.exception.NoSuitableCoursesException;
import utc.englishlearning.Encybara.service.helper.RecommendationRange;
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
     * Creates recommendations for a user with adaptive range to ensure at least one
     * enrollment is created
     */
    public List<Enrollment> createAdaptiveRecommendations(User user, Learning_Result learningResult) {
        RecommendationRange range = new RecommendationRange(
                (learningResult.getListeningScore() + learningResult.getSpeakingScore() +
                        learningResult.getReadingScore() + learningResult.getWritingScore()) / 4.0);

        List<Course> recommendedCourses;
        List<Enrollment> createdEnrollments = new java.util.ArrayList<>();

        // Keep trying with increasing range until we get at least one valid course
        do {
            try {
                recommendedCourses = courseRecommendationService.getRecommendedCoursesWithRange(
                        learningResult, range.getLowerBound(), range.getUpperBound());

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
                recommendedCourses = java.util.Collections.emptyList();
            }

            // If no enrollments were created and we can't increase range anymore, throw
            // exception
            if (createdEnrollments.isEmpty() && !range.increaseRange()) {
                throw new NoSuitableCoursesException(
                        "No suitable courses found even with expanded difficulty range");
            }

        } while (createdEnrollments.isEmpty());

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