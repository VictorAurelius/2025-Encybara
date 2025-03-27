package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.EnrollmentRepository;
import utc.englishlearning.Encybara.exception.DuplicateEnrollmentException;

@Service
public class EnrollmentHelper {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

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