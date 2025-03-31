package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.request.enrollment.ReqCreateEnrollmentDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentWithRecommendationsDTO.CourseRecommendation;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.EnglishLevelEnum;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private EnrollmentHelper enrollmentHelper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LessonResultRepository lessonResultRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private LearningResultService learningResultService;
    @Autowired
    private LearningResultRepository learningResultRepository;

    /**
     * Creates a new course enrollment for a user
     */
    @Transactional
    public ResEnrollmentDTO createEnrollment(ReqCreateEnrollmentDTO reqCreateEnrollmentDTO) {
        User user = userRepository.findById(reqCreateEnrollmentDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Course course = courseRepository.findById(reqCreateEnrollmentDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Learning_Result learningResult = getOrCreateLearningResult(user);
        Enrollment enrollment = enrollmentHelper.createCourseEnrollment(user, course, learningResult, true);
        return convertToDTO(enrollment);
    }

    /**
     * Step 1: Calculate and save completion info with validation
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResEnrollmentDTO saveEnrollmentCompletion(Long enrollmentId) {
        try {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

            if (!enrollment.isProStatus()) {
                throw new IllegalStateException("Cannot complete a non-active enrollment");
            }

            List<Lesson> lessons = enrollment.getCourse().getLessons();
            if (lessons.isEmpty()) {
                throw new IllegalStateException("Course has no lessons to complete");
            }

            int totalPointsPossible = calculateTotalPointsPossible(lessons);
            if (totalPointsPossible == 0) {
                throw new IllegalStateException("Course has no points available");
            }

            int totalPointsAchieved = calculateTotalPointsAchieved(enrollment);
            if (totalPointsAchieved > totalPointsPossible) {
                throw new IllegalStateException("Achieved points cannot exceed possible points");
            }

            double comLevel = (double) totalPointsAchieved / totalPointsPossible * 100;
            double skillScore = (totalPointsAchieved * 100.0) / totalPointsPossible;

            enrollment.setTotalPoints(totalPointsAchieved);
            enrollment.setComLevel(comLevel);
            enrollment.setSkillScore(skillScore);
            enrollmentRepository.save(enrollment);

            return convertToDTO(enrollment);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save enrollment completion: " + e.getMessage(), e);
        }
    }

    /**
     * Step 2: Update learning results with validation
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateLearningResults(Long enrollmentId) {
        try {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

            if (enrollment.getComLevel() < 80.0) {
                throw new IllegalStateException("Must complete course (80%+) before updating learning results");
            }

            Learning_Result learningResult = enrollment.getLearningResult();
            if (learningResult == null) {
                throw new IllegalStateException("No learning result found for enrollment");
            }

            double prevListening = learningResult.getListeningScore();
            double prevSpeaking = learningResult.getSpeakingScore();
            double prevReading = learningResult.getReadingScore();
            double prevWriting = learningResult.getWritingScore();

            learningResultService.evaluateAndUpdateScores(enrollment);
            validateScoreChanges(learningResult, prevListening, prevSpeaking, prevReading, prevWriting);

            double avgScore = (learningResult.getListeningScore() +
                    learningResult.getSpeakingScore() +
                    learningResult.getReadingScore() +
                    learningResult.getWritingScore()) / 4.0;

            User user = enrollment.getUser();
            EnglishLevelEnum level = EnglishLevelEnum.fromScore(avgScore);
            user.setEnglishlevel(level.getDisplayName());
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update learning results: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void joinCourse(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        enrollment.setProStatus(true);
        enrollment.setEnrollDate(Instant.now());
        enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void refuseCourse(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        enrollmentRepository.delete(enrollment);
    }

    public Page<ResEnrollmentDTO> getEnrollmentsByUserId(Long userId, Boolean proStatus, Pageable pageable) {
        Page<Enrollment> enrollments = proStatus != null
                ? enrollmentRepository.findByUserIdAndProStatus(userId, proStatus, pageable)
                : enrollmentRepository.findByUserId(userId, pageable);
        return enrollments.map(this::convertToDTO);
    }

    public ResEnrollmentDTO getLatestEnrollmentByCourseIdAndUserId(Long courseId, Long userId) {
        List<Enrollment> enrollments = enrollmentRepository
                .findTopByCourseIdAndUserIdOrderByEnrollDateDesc(courseId, userId, PageRequest.of(0, 1));

        Enrollment enrollment = enrollments.isEmpty() ? null : enrollments.get(0);
        if (enrollment == null) {
            throw new ResourceNotFoundException("No enrollment found for this course and user");
        }
        return convertToDTO(enrollment);
    }

    private void validateScoreChanges(Learning_Result learningResult,
            double prevListening, double prevSpeaking, double prevReading, double prevWriting) {
        if (learningResult.getListeningScore() < prevListening ||
                learningResult.getSpeakingScore() < prevSpeaking ||
                learningResult.getReadingScore() < prevReading ||
                learningResult.getWritingScore() < prevWriting) {
            throw new IllegalStateException("Skill scores cannot decrease on course completion");
        }
    }

    private Learning_Result getOrCreateLearningResult(User user) {
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

        return learningResult;
    }

    private ResEnrollmentDTO convertToDTO(Enrollment enrollment) {
        ResEnrollmentDTO dto = new ResEnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUser().getId());
        dto.setCourseId(enrollment.getCourse().getId());
        dto.setEnrollDate(enrollment.getEnrollDate());
        dto.setProStatus(enrollment.isProStatus());
        dto.setTotalPoints(enrollment.getTotalPoints());
        dto.setComLevel(enrollment.getComLevel());
        return dto;
    }

    private int calculateTotalPointsPossible(List<Lesson> lessons) {
        return lessons.stream()
                .mapToInt(lesson -> questionRepository.findByLesson(lesson).stream()
                        .mapToInt(Question::getPoint)
                        .sum())
                .sum();
    }

    private int calculateTotalPointsAchieved(Enrollment enrollment) {
        Map<Long, Integer> maxPointsByLesson = new HashMap<>();
        List<Lesson_Result> lessonResults = lessonResultRepository.findByEnrollment(enrollment);

        for (Lesson_Result result : lessonResults) {
            Long lessonId = result.getLesson().getId();
            maxPointsByLesson.put(lessonId,
                    Math.max(maxPointsByLesson.getOrDefault(lessonId, 0), result.getTotalPoints()));
        }

        return maxPointsByLesson.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Step 3: Create course recommendations with validation
     * Uses SERIALIZABLE isolation to prevent concurrent recommendation creation
     * 
     * @param enrollmentId ID of the completed enrollment
     * @return List of recommended courses
     * @throws ResourceNotFoundException if enrollment not found
     * @throws IllegalStateException     if validation fails
     */
    /**
     * Creates course recommendations based on user's learning results
     * Uses optimistic locking with READ_COMMITTED isolation to minimize contention
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CourseRecommendation> createRecommendations(Long enrollmentId) {
        try {
            // Get enrollment data
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

            validateEnrollmentForRecommendations(enrollment);
            User user = enrollment.getUser();
            Learning_Result learningResult = enrollment.getLearningResult();

            // Delete existing recommendations first
            deleteExistingRecommendations(user);

            // Calculate current level
            double avgScore = (learningResult.getListeningScore() +
                    learningResult.getSpeakingScore() +
                    learningResult.getReadingScore() +
                    learningResult.getWritingScore()) / 4.0;

            try {
                // Generate recommendations
                List<Enrollment> recommendations = enrollmentHelper.createProgressiveRecommendations(
                        user, learningResult, avgScore);

                // Convert and return
                return recommendations.stream()
                        .map(e -> createCourseRecommendation(e.getCourse(), learningResult))
                        .collect(Collectors.toList());

            } catch (Exception e) {
                // Handle specific database errors gracefully
                if (e.getMessage() != null &&
                        (e.getMessage().contains("Duplicate entry") ||
                                e.getMessage().contains("deadlock") ||
                                e.getMessage().contains("Lock wait timeout"))) {
                    return new ArrayList<>(); // Return empty list for concurrent modifications
                }
                throw new RuntimeException("Failed to create recommendations: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recommendations: " + e.getMessage(), e);
        }
    }

    /**
     * Validates an enrollment is suitable for recommendations
     */
    private void validateEnrollmentForRecommendations(Enrollment enrollment) {
        if (!enrollment.isProStatus()) {
            throw new IllegalStateException("Cannot create recommendations for non-active enrollment");
        }

        if (enrollment.getComLevel() < 80.0) {
            throw new IllegalStateException("Cannot create recommendations for incomplete course");
        }

        Learning_Result learningResult = enrollment.getLearningResult();
        if (learningResult == null) {
            throw new IllegalStateException("Learning result not found");
        }

        validateLearningResultScores(learningResult);

        if (learningResult.getLastUpdated() == null ||
                learningResult.getLastUpdated().isBefore(enrollment.getEnrollDate())) {
            throw new IllegalStateException("Learning results must be updated before getting recommendations");
        }
    }

    /**
     * Validates all scores in a learning result
     */
    private void validateLearningResultScores(Learning_Result learningResult) {
        validateScore(learningResult.getListeningScore(), "Listening");
        validateScore(learningResult.getSpeakingScore(), "Speaking");
        validateScore(learningResult.getReadingScore(), "Reading");
        validateScore(learningResult.getWritingScore(), "Writing");
        validateScore(learningResult.getPreviousListeningScore(), "Previous Listening");
        validateScore(learningResult.getPreviousSpeakingScore(), "Previous Speaking");
        validateScore(learningResult.getPreviousReadingScore(), "Previous Reading");
        validateScore(learningResult.getPreviousWritingScore(), "Previous Writing");
    }

    /**
     * Validates that a score is within valid range
     */
    private double validateScore(double score, String skillName) {
        if (score < 0 || score > 7) {
            throw new IllegalStateException(
                    String.format("%s score (%.2f) must be between 0 and 7", skillName, score));
        }
        return score;
    }

    /**
     * Creates a course recommendation DTO
     */
    private CourseRecommendation createCourseRecommendation(Course course, Learning_Result learningResult) {
        CourseRecommendation recommendation = new CourseRecommendation();
        recommendation.setCourseId(course.getId());
        recommendation.setCourseName(course.getName());
        recommendation.setCourseType(course.getCourseType());
        recommendation.setDiffLevel(course.getDiffLevel());
        recommendation.setReason(generateRecommendationReason(course, learningResult));
        return recommendation;
    }

    /**
     * Generates a reason for the recommendation based on difficulty level
     * difference
     */
    private String generateRecommendationReason(Course course, Learning_Result learningResult) {
        double currentLevel = switch (course.getCourseType()) {
            case LISTENING -> learningResult.getListeningScore();
            case SPEAKING -> learningResult.getSpeakingScore();
            case READING -> learningResult.getReadingScore();
            case WRITING -> learningResult.getWritingScore();
            case ALLSKILLS -> (learningResult.getListeningScore() + learningResult.getSpeakingScore() +
                    learningResult.getReadingScore() + learningResult.getWritingScore()) / 4.0;
        };

        if (course.getDiffLevel() > currentLevel + 0.5) {
            return "Challenging course to advance your skills";
        } else if (course.getDiffLevel() < currentLevel - 0.5) {
            return "Recommended for skill reinforcement";
        }
        return "Matches your current level for optimal learning";
    }

    /**
     * Handles deletion of existing recommendations in a separate transaction
     * with READ_COMMITTED isolation to prevent lock contention
     */
    /**
     * Delete existing recommendations in batches to avoid timeouts
     */
    /**
     * Delete existing recommendations with pagination to avoid memory issues
     */
    private void deleteExistingRecommendations(User user) {
        // Use pageable to handle large datasets
        Page<Enrollment> page = enrollmentRepository.findByUserIdAndProStatus(
                user.getId(), false, PageRequest.of(0, 50));

        if (!page.hasContent()) {
            return;
        }

        // Delete each enrollment
        for (Enrollment enrollment : page.getContent()) {
            try {
                enrollmentRepository.delete(enrollment);
            } catch (Exception e) {
                // Log and continue if single delete fails
                System.err.println("Failed to delete enrollment " + enrollment.getId() + ": " + e.getMessage());
            }
        }
        enrollmentRepository.flush();
    }
}