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

    @Autowired
    private PlacementAssessmentService placementAssessmentService;

    @Autowired
    private EnrollmentHelper enrollmentHelper;

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
                    enrollmentHelper.createCourseEnrollment(user, course, learningResult, false);
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

            // Create enrollment for assessment course with proStatus=true
            enrollmentHelper.createCourseEnrollment(user, assessmentCourse, learningResult, true);
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

            // Calculate skill scores from total points
            PlacementAssessmentService.SkillScores scores = placementAssessmentService
                    .calculateSkillScoresFromTotalPoints(
                            request.getListeningTotalPoints(),
                            request.getSpeakingTotalPoints(),
                            request.getReadingTotalPoints(),
                            request.getWritingTotalPoints());

            // Set skill-specific score based on course type
            switch (placementEnrollment.getCourse().getCourseType()) {
                case LISTENING -> placementEnrollment.setSkillScore(scores.getListeningScore());
                case SPEAKING -> placementEnrollment.setSkillScore(scores.getSpeakingScore());
                case READING -> placementEnrollment.setSkillScore(scores.getReadingScore());
                case WRITING -> placementEnrollment.setSkillScore(scores.getWritingScore());
                default -> placementEnrollment.setSkillScore(
                        (scores.getListeningScore() + scores.getSpeakingScore() +
                                scores.getReadingScore() + scores.getWritingScore()) / 4.0);
            }

            // Update learning result with calculated scores
            Learning_Result learningResult = placementEnrollment.getLearningResult();
            learningResult.setListeningScore(scores.getListeningScore());
            learningResult.setSpeakingScore(scores.getSpeakingScore());
            learningResult.setReadingScore(scores.getReadingScore());
            learningResult.setWritingScore(scores.getWritingScore());

            // Set previous scores same as current for initial assessment
            learningResult.setPreviousListeningScore(scores.getListeningScore());
            learningResult.setPreviousSpeakingScore(scores.getSpeakingScore());
            learningResult.setPreviousReadingScore(scores.getReadingScore());
            learningResult.setPreviousWritingScore(scores.getWritingScore());

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
                    enrollmentHelper.createCourseEnrollment(user, course, learningResult, false);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to complete placement assessment: " + e.getMessage(), e);
        }
    }
}