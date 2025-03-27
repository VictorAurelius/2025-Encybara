package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.EnglishLevelEnum;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserEnglishLevelService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LearningResultRepository learningResultRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

    @Autowired
    private EnrollmentHelper enrollmentHelper;

    @Transactional
    public void setUserEnglishLevel(Long userId, EnglishLevelEnum level) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Get or create learning result
            Learning_Result learningResult = user.getLearningResult();
            if (learningResult == null) {
                learningResult = new Learning_Result();
                learningResult.setUser(user);
            }

            // Set scores based on selected level
            double baseScore = (level.getMinScore() + level.getMaxScore()) / 2;
            learningResult.setListeningScore(baseScore);
            learningResult.setSpeakingScore(baseScore);
            learningResult.setReadingScore(baseScore);
            learningResult.setWritingScore(baseScore);
            learningResult.setPreviousListeningScore(baseScore);
            learningResult.setPreviousSpeakingScore(baseScore);
            learningResult.setPreviousReadingScore(baseScore);
            learningResult.setPreviousWritingScore(baseScore);
            learningResult.setLastUpdated(Instant.now());

            learningResult = learningResultRepository.save(learningResult);

            // Delete old course recommendations (enrollments with proStatus = false)
            enrollmentRepository.deleteByUserAndProStatusFalse(user);

            // Get new course recommendations based on updated scores
            var recommendedCourses = courseRecommendationService.getRecommendedCourses(learningResult);

            // Create new enrollment entries for recommendations
            for (Course course : recommendedCourses) {
                if (!course.getName().contains("(Placement)")) { // Skip placement course
                    // Create enrollment with proStatus=false for recommendations
                    enrollmentHelper.createCourseEnrollment(user, course, learningResult, false);
                }
            }

            // Update user's English level display name
            user.setEnglishlevel(level.getDisplayName());
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user English level: " + e.getMessage(), e);
        }
    }

    public EnglishLevelDTO getUserEnglishLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String userLevel = user.getEnglishlevel();

        // Find the matching enum and convert to DTO
        return Arrays.stream(EnglishLevelEnum.values())
                .filter(level -> level.getDisplayName().equals(userLevel))
                .findFirst()
                .map(level -> new EnglishLevelDTO(
                        level.name(),
                        level.getDisplayName(),
                        level.getMinScore(),
                        level.getMaxScore()))
                .orElse(null);
    }

    public List<EnglishLevelDTO> getAllEnglishLevels() {
        return Arrays.stream(EnglishLevelEnum.values())
                .map(level -> new EnglishLevelDTO(
                        level.name(),
                        level.getDisplayName(),
                        level.getMinScore(),
                        level.getMaxScore()))
                .collect(Collectors.toList());
    }

    public static class EnglishLevelDTO {
        private String name;
        private String displayName;
        private double minScore;
        private double maxScore;

        public EnglishLevelDTO(String name, String displayName, double minScore, double maxScore) {
            this.name = name;
            this.displayName = displayName;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public double getMinScore() {
            return minScore;
        }

        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }

        public double getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(double maxScore) {
            this.maxScore = maxScore;
        }
    }
}