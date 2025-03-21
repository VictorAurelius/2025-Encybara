package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.request.enrollment.ReqCreateEnrollmentDTO;
import utc.englishlearning.Encybara.domain.request.enrollment.ReqCalculateEnrollmentResultDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentWithRecommendationsDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentWithRecommendationsDTO.CourseRecommendation;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentWithRecommendationsDTO.SkillProgress;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonResultRepository lessonResultRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private LearningResultService learningResultService;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

    @Transactional
    public ResEnrollmentDTO createEnrollment(ReqCreateEnrollmentDTO reqCreateEnrollmentDTO) {
        User user = userRepository.findById(reqCreateEnrollmentDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Course course = courseRepository.findById(reqCreateEnrollmentDTO.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        enrollment.setEnrollDate(Instant.now());
        enrollment.setProStatus(false);

        enrollment = enrollmentRepository.save(enrollment);
        return convertToDTO(enrollment);
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
        Page<Enrollment> enrollments;
        if (proStatus != null) {
            enrollments = enrollmentRepository.findByUserIdAndProStatus(userId, proStatus, pageable);
        } else {
            enrollments = enrollmentRepository.findByUserId(userId, pageable);
        }
        return enrollments.map(this::convertToDTO);
    }

    @Transactional
    public ResEnrollmentWithRecommendationsDTO calculateEnrollmentResult(ReqCalculateEnrollmentResultDTO reqDto) {
        Enrollment enrollment = enrollmentRepository.findById(reqDto.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        // Calculate total points possible and achieved
        List<Lesson> lessons = enrollment.getCourse().getLessons();
        int totalPointsPossible = calculateTotalPointsPossible(lessons);
        int totalPointsAchieved = calculateTotalPointsAchieved(enrollment);

        // Calculate completion level
        double comLevel = totalPointsPossible > 0 ? (double) totalPointsAchieved / totalPointsPossible * 100 : 0;

        // Update enrollment
        enrollment.setTotalPoints(totalPointsAchieved);
        enrollment.setComLevel(comLevel);
        enrollment = enrollmentRepository.save(enrollment);

        // Create enhanced response DTO
        ResEnrollmentWithRecommendationsDTO response = new ResEnrollmentWithRecommendationsDTO();
        response.setEnrollmentId(enrollment.getId());
        response.setTotalPoints(totalPointsAchieved);
        response.setComLevel(comLevel);

        // Automatically evaluate learning result when course is completed
        if (comLevel >= 80.0) {
            // Update learning result scores
            learningResultService.evaluateAndUpdateScores(enrollment);
            Learning_Result learningResult = enrollment.getLearningResult();

            // Get course recommendations
            List<Course> recommendedCourses = courseRecommendationService.getRecommendedCourses(learningResult);

            // Map recommendations to DTOs
            response.setRecommendations(recommendedCourses.stream()
                    .map(course -> createCourseRecommendation(course, learningResult))
                    .collect(Collectors.toList()));

            // Calculate and set skill progress
            response.setSkillProgress(calculateSkillProgress(learningResult));
        }

        return response;
    }

    private CourseRecommendation createCourseRecommendation(Course course, Learning_Result learningResult) {
        CourseRecommendation recommendation = new CourseRecommendation();
        recommendation.setCourseId(course.getId());
        recommendation.setCourseName(course.getName());
        recommendation.setCourseType(course.getCourseType());
        recommendation.setDiffLevel(course.getDiffLevel());

        // Generate personalized reason based on skill levels and course type
        String reason = generateRecommendationReason(course, learningResult);
        recommendation.setReason(reason);

        return recommendation;
    }

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
        } else {
            return "Matches your current level for optimal learning";
        }
    }

    private SkillProgress calculateSkillProgress(Learning_Result learningResult) {
        SkillProgress progress = new SkillProgress();

        // Calculate progress for each skill
        progress.setListeningProgress(learningResult.getListeningScore() - learningResult.getPreviousListeningScore());
        progress.setSpeakingProgress(learningResult.getSpeakingScore() - learningResult.getPreviousSpeakingScore());
        progress.setReadingProgress(learningResult.getReadingScore() - learningResult.getPreviousReadingScore());
        progress.setWritingProgress(learningResult.getWritingScore() - learningResult.getPreviousWritingScore());

        // Determine focus area (skill with lowest score)
        Map<String, Double> skillScores = new HashMap<>();
        skillScores.put("Listening", learningResult.getListeningScore());
        skillScores.put("Speaking", learningResult.getSpeakingScore());
        skillScores.put("Reading", learningResult.getReadingScore());
        skillScores.put("Writing", learningResult.getWritingScore());

        progress.setFocusArea(skillScores.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null));

        progress.setStrongestSkill(skillScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null));

        return progress;
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

    public ResEnrollmentDTO getLatestEnrollmentByCourseIdAndUserId(Long courseId, Long userId) {
        List<Enrollment> enrollments = enrollmentRepository
                .findTopByCourseIdAndUserIdOrderByEnrollDateDesc(courseId, userId, PageRequest.of(0, 1));

        Enrollment enrollment = enrollments.isEmpty() ? null : enrollments.get(0);
        if (enrollment == null) {
            throw new ResourceNotFoundException("No enrollment found for this course and user");
        }
        return convertToDTO(enrollment);
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
}