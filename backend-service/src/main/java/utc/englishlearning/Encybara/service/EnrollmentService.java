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
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;

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
        // Get all lessons from the course
        List<Lesson> courseLessons = enrollment.getCourse().getLessons();

        // Get all lesson results for this enrollment
        List<Lesson_Result> lessonResults = lessonResultRepository.findByEnrollment(enrollment);

        // Group results by lessonId and get the one with highest sessionId for each
        // lesson
        Map<Long, Lesson_Result> latestResultsByLesson = new HashMap<>();

        for (Lesson_Result result : lessonResults) {
            Long lessonId = result.getLesson().getId();
            Lesson_Result existing = latestResultsByLesson.get(lessonId);

            // Update if this is the first result or has a higher sessionId
            if (existing == null || result.getSessionId() > existing.getSessionId()) {
                latestResultsByLesson.put(lessonId, result);
            }
        }

        // Sum up points only from the latest attempts
        int totalPoints = 0;
        for (Lesson lesson : courseLessons) {
            Lesson_Result latestResult = latestResultsByLesson.get(lesson.getId());
            if (latestResult != null) {
                totalPoints += latestResult.getTotalPoints();
            }
        }

        return totalPoints;
    }

    /**
     * Step 3: Create course recommendations with validation
     * 
     * @param enrollmentId ID of the completed enrollment
     * @return List of recommended courses
     * @throws ResourceNotFoundException if enrollment not found
     * @throws IllegalStateException     if validation fails
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CourseRecommendation> createRecommendations(Long enrollmentId) {
        try {
            // Get and validate enrollment
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

            if (!enrollment.isProStatus()) {
                throw new IllegalStateException("Cannot create recommendations for non-active enrollment");
            }

            // Get learning result
            Learning_Result learningResult = enrollment.getLearningResult();
            if (learningResult == null) {
                throw new IllegalStateException("No learning result found for enrollment");
            }

            // Calculate base score from current learning results
            double baseScore = (learningResult.getListeningScore() +
                    learningResult.getSpeakingScore() +
                    learningResult.getReadingScore() +
                    learningResult.getWritingScore()) / 4.0;

            User user = enrollment.getUser();

            // Delete old recommendations before creating new ones
            enrollmentRepository.deleteByUserAndProStatusFalse(user);

            // Create new recommendations
            try {
                // Try to create progressive recommendations
                return enrollmentHelper.createProgressiveRecommendations(user, learningResult, baseScore)
                        .stream()
                        .map(this::convertToCourseRecommendation)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                // Fallback: Try to find courses matching the user's strongest skill from step 2
                double listeningLevel = learningResult.getListeningScore();
                double speakingLevel = learningResult.getSpeakingScore();
                double readingLevel = learningResult.getReadingScore();
                double writingLevel = learningResult.getWritingScore();

                // Find the strongest skill
                CourseTypeEnum recommendedType = CourseTypeEnum.ALLSKILLS;
                double highestScore = baseScore;

                if (listeningLevel > highestScore) {
                    highestScore = listeningLevel;
                    recommendedType = CourseTypeEnum.LISTENING;
                }
                if (speakingLevel > highestScore) {
                    highestScore = speakingLevel;
                    recommendedType = CourseTypeEnum.SPEAKING;
                }
                if (readingLevel > highestScore) {
                    highestScore = readingLevel;
                    recommendedType = CourseTypeEnum.READING;
                }
                if (writingLevel > highestScore) {
                    highestScore = writingLevel;
                    recommendedType = CourseTypeEnum.WRITING;
                }

                // Try to find a course matching the strongest skill
                List<Course> matchingCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                        recommendedType,
                        highestScore - 0.5,
                        highestScore + 0.5,
                        CourseStatusEnum.PUBLIC);

                if (!matchingCourses.isEmpty()) {
                    Course course = matchingCourses.get(0);
                    Enrollment matchingEnrollment = enrollmentHelper.createCourseEnrollment(
                            user, course, learningResult, false);

                    return Collections.singletonList(convertToCourseRecommendation(matchingEnrollment));
                }

                // Ultimate fallback: Try to find a course that matches the most improved skill
                double listeningImprovement = learningResult.getListeningScore()
                        - learningResult.getPreviousListeningScore();
                double speakingImprovement = learningResult.getSpeakingScore()
                        - learningResult.getPreviousSpeakingScore();
                double readingImprovement = learningResult.getReadingScore() - learningResult.getPreviousReadingScore();
                double writingImprovement = learningResult.getWritingScore() - learningResult.getPreviousWritingScore();

                CourseTypeEnum improvedType = CourseTypeEnum.ALLSKILLS;
                double bestImprovement = 0;
                double skillLevel = baseScore;

                if (listeningImprovement > bestImprovement) {
                    bestImprovement = listeningImprovement;
                    improvedType = CourseTypeEnum.LISTENING;
                    skillLevel = listeningLevel;
                }
                if (speakingImprovement > bestImprovement) {
                    bestImprovement = speakingImprovement;
                    improvedType = CourseTypeEnum.SPEAKING;
                    skillLevel = speakingLevel;
                }
                if (readingImprovement > bestImprovement) {
                    bestImprovement = readingImprovement;
                    improvedType = CourseTypeEnum.READING;
                    skillLevel = readingLevel;
                }
                if (writingImprovement > bestImprovement) {
                    bestImprovement = writingImprovement;
                    improvedType = CourseTypeEnum.WRITING;
                    skillLevel = writingLevel;
                }

                List<Course> fallbackCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                        improvedType,
                        skillLevel - 0.5,
                        skillLevel + 0.5,
                        CourseStatusEnum.PUBLIC);

                if (!fallbackCourses.isEmpty()) {
                    Enrollment fallbackEnrollment = enrollmentHelper.createCourseEnrollment(
                            user, fallbackCourses.get(0), learningResult, false);
                    return Collections.singletonList(convertToCourseRecommendation(fallbackEnrollment));
                }

                // If all else fails, return an ALLSKILLS course at the current level
                List<Course> finalFallbackCourses = courseRepository.findPublicCoursesByTypeAndLevelRange(
                        CourseTypeEnum.ALLSKILLS,
                        baseScore - 0.5,
                        baseScore + 0.5,
                        CourseStatusEnum.PUBLIC);

                if (finalFallbackCourses.isEmpty()) {
                    throw new IllegalStateException("No suitable courses available for recommendations");
                }

                Enrollment finalFallback = enrollmentHelper.createCourseEnrollment(
                        user, finalFallbackCourses.get(0), learningResult, false);
                return Collections.singletonList(convertToCourseRecommendation(finalFallback));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create recommendations: " + e.getMessage(), e);
        }
    }

    private CourseRecommendation convertToCourseRecommendation(Enrollment enrollment) {
        CourseRecommendation recommendation = new CourseRecommendation();
        recommendation.setCourseId(enrollment.getCourse().getId());
        recommendation.setCourseName(enrollment.getCourse().getName());
        recommendation.setCourseType(enrollment.getCourse().getCourseType());
        recommendation.setDiffLevel(enrollment.getCourse().getDiffLevel());

        Learning_Result lr = enrollment.getLearningResult();
        CourseTypeEnum courseType = enrollment.getCourse().getCourseType();

        String reason;
        if (courseType == CourseTypeEnum.ALLSKILLS) {
            reason = String.format(
                    "Comprehensive course matching your current skill levels - Listening: %.1f, Speaking: %.1f, Reading: %.1f, Writing: %.1f",
                    lr.getListeningScore(), lr.getSpeakingScore(), lr.getReadingScore(), lr.getWritingScore());
        } else {
            double currentScore;
            double previousScore;
            String skillName;

            switch (courseType) {
                case LISTENING:
                    currentScore = lr.getListeningScore();
                    previousScore = lr.getPreviousListeningScore();
                    skillName = "Listening";
                    break;
                case SPEAKING:
                    currentScore = lr.getSpeakingScore();
                    previousScore = lr.getPreviousSpeakingScore();
                    skillName = "Speaking";
                    break;
                case READING:
                    currentScore = lr.getReadingScore();
                    previousScore = lr.getPreviousReadingScore();
                    skillName = "Reading";
                    break;
                case WRITING:
                    currentScore = lr.getWritingScore();
                    previousScore = lr.getPreviousWritingScore();
                    skillName = "Writing";
                    break;
                default:
                    currentScore = (lr.getListeningScore() + lr.getSpeakingScore() + lr.getReadingScore()
                            + lr.getWritingScore()) / 4.0;
                    previousScore = currentScore;
                    skillName = "Overall";
            }

            double improvement = currentScore - previousScore;
            if (improvement > 0) {
                reason = String.format(
                        "Course focused on %s skill (Score: %.1f, Improved: +%.1f). Course difficulty: %.1f",
                        skillName, currentScore, improvement, enrollment.getCourse().getDiffLevel());
            } else {
                reason = String.format(
                        "Course to enhance your %s skill (Current: %.1f). Course difficulty: %.1f",
                        skillName, currentScore, enrollment.getCourse().getDiffLevel());
            }
        }

        recommendation.setReason(reason);
        return recommendation;
    }
}