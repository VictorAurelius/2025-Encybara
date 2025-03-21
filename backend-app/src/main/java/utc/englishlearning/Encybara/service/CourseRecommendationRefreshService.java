package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;

import java.time.Instant;
import java.util.List;

@Service
public class CourseRecommendationRefreshService {

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

        // Get new course recommendations
        List<Course> recommendedCourses = courseRecommendationService.getRecommendedCourses(learningResult);

        // Create new enrollment recommendations
        for (Course course : recommendedCourses) {
            // Skip if user already has an active enrollment for this course
            if (hasActiveEnrollment(user, course)) {
                continue;
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(course);
            enrollment.setEnrollDate(Instant.now());
            enrollment.setProStatus(false); // Not started
            enrollment.setComLevel(0.0); // No progress yet
            enrollment.setTotalPoints(0); // No points yet
            enrollment.setLearningResult(learningResult);

            enrollmentRepository.save(enrollment);
        }

        System.out.println("Refreshed recommendations for user " + user.getId() +
                " - Created " + recommendedCourses.size() + " new recommendations");
    }

    private boolean hasActiveEnrollment(User user, Course course) {
        return enrollmentRepository.existsByUserAndCourseAndProStatusTrue(user, course);
    }
}