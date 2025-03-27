package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.request.assessment.ReqCompletePlacementDTO;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.exception.InvalidOperationException;

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
                if (!course.getName().contains("(Placement)")) { // Skip placement course
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

            // Find placement assessment course
            Page<Course> placementCourses = courseRepository.findCoursesWithFilters(
                    null, null, null, null, null, "PLACEMENT", null, PageRequest.of(0, 1));

            Course assessmentCourse = placementCourses.getContent()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Placement assessment course not found"));

            // Check if user already has an enrollment for this course
            if (enrollmentRepository.findByUserIdAndCourseId(user.getId(), assessmentCourse.getId()).isPresent()) {
                throw new RuntimeException("User already enrolled in placement assessment course");
            }

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

    @Transactional
    public void completePlacementAssessment(ReqCompletePlacementDTO request) {
        try {
            // Get enrollment by id
            Enrollment placementEnrollment = enrollmentRepository.findById(request.getEnrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

            // Validate completion level
            if (request.getComLevel() > 100.0) {
                throw new InvalidOperationException("Completion level cannot exceed 100%");
            }

            // Update enrollment with frontend-provided values
            placementEnrollment.setTotalPoints(request.getTotalPoints());
            placementEnrollment.setComLevel(request.getComLevel());

            // Set skill-specific score based on course type
            switch (placementEnrollment.getCourse().getCourseType()) {
                case LISTENING -> placementEnrollment.setSkillScore(request.getListeningScore());
                case SPEAKING -> placementEnrollment.setSkillScore(request.getSpeakingScore());
                case READING -> placementEnrollment.setSkillScore(request.getReadingScore());
                case WRITING -> placementEnrollment.setSkillScore(request.getWritingScore());
                default -> placementEnrollment.setSkillScore((request.getListeningScore() + request.getSpeakingScore()
                        + request.getReadingScore() + request.getWritingScore()) / 4.0);
            }

            // Update learning result with frontend-provided scores
            Learning_Result learningResult = placementEnrollment.getLearningResult();
            learningResult.setListeningScore(request.getListeningScore());
            learningResult.setSpeakingScore(request.getSpeakingScore());
            learningResult.setReadingScore(request.getReadingScore());
            learningResult.setWritingScore(request.getWritingScore());

            // Set previous scores same as current for initial assessment
            learningResult.setPreviousListeningScore(request.getListeningScore());
            learningResult.setPreviousSpeakingScore(request.getSpeakingScore());
            learningResult.setPreviousReadingScore(request.getReadingScore());
            learningResult.setPreviousWritingScore(request.getWritingScore());

            learningResult.setLastUpdated(Instant.now());

            // Save both enrollment and learning result
            learningResultRepository.save(learningResult);
            enrollmentRepository.save(placementEnrollment);

            // Get course recommendations based on placement scores
            var recommendedCourses = courseRecommendationService.getRecommendedCourses(learningResult);

            // Create enrollment entries for recommendations
            User user = placementEnrollment.getUser();
            for (Course course : recommendedCourses) {
                if (!course.getName().contains("(Placement)")) { // Skip placement course
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
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to complete placement assessment: " + e.getMessage(), e);
        }
    }
}