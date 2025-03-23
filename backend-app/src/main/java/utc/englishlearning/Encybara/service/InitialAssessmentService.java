package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;

import java.time.Instant;

@Service
public class InitialAssessmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearningResultRepository learningResultRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

    @Transactional
    public void skipInitialAssessment(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Get existing learning result or create new one
            Learning_Result learningResult = user.getLearningResult();
            if (learningResult == null) {
                learningResult = new Learning_Result();
                learningResult.setUser(user);
                learningResult.setListeningScore(1.0);
                learningResult.setSpeakingScore(1.0);
                learningResult.setReadingScore(1.0);
                learningResult.setWritingScore(1.0);
                learningResult.setPreviousListeningScore(1.0);
                learningResult.setPreviousSpeakingScore(1.0);
                learningResult.setPreviousReadingScore(1.0);
                learningResult.setPreviousWritingScore(1.0);
                learningResult.setLastUpdated(Instant.now());
                learningResult = learningResultRepository.save(learningResult);
            }

            // Get course recommendations based on base scores
            var recommendedCourses = courseRecommendationService.getRecommendedCourses(learningResult);

            // Create enrollment entries for recommendations
            for (Course course : recommendedCourses) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                enrollment.setLearningResult(learningResult);
                enrollment.setEnrollDate(Instant.now());
                enrollment.setProStatus(false);
                enrollment.setComLevel(0.0);
                enrollment.setTotalPoints(0);

                enrollmentRepository.save(enrollment);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to skip initial assessment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void startInitialAssessment(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            Course assessmentCourse = courseRepository.findById(1L) // Assessment course has ID 1
                    .orElseThrow(() -> new ResourceNotFoundException("Assessment course not found with ID: 1"));

            // Get existing learning result or create new one
            Learning_Result learningResult = user.getLearningResult();
            if (learningResult == null) {
                learningResult = new Learning_Result();
                learningResult.setUser(user);
                learningResult.setListeningScore(1.0);
                learningResult.setSpeakingScore(1.0);
                learningResult.setReadingScore(1.0);
                learningResult.setWritingScore(1.0);
                learningResult.setPreviousListeningScore(1.0);
                learningResult.setPreviousSpeakingScore(1.0);
                learningResult.setPreviousReadingScore(1.0);
                learningResult.setPreviousWritingScore(1.0);
                learningResult.setLastUpdated(Instant.now());
                learningResult = learningResultRepository.save(learningResult);
            }

            // Create enrollment for assessment course
            Enrollment enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setCourse(assessmentCourse);
            enrollment.setEnrollDate(Instant.now());
            enrollment.setProStatus(true); // User is actively taking this course
            enrollment.setComLevel(0.0);
            enrollment.setTotalPoints(0);
            enrollment.setLearningResult(learningResult);

            enrollmentRepository.save(enrollment);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to start initial assessment: " + e.getMessage(), e);
        }
    }
}