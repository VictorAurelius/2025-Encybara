package utc.englishlearning.Encybara.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import utc.englishlearning.Encybara.domain.Learning_Result;
import utc.englishlearning.Encybara.domain.Enrollment;
import utc.englishlearning.Encybara.domain.request.learning_result.ReqUpdateLearningResultDTO;
import utc.englishlearning.Encybara.domain.response.learning_result.ResLearningResultDTO;
import utc.englishlearning.Encybara.domain.response.learning_result.ResDetailedLearningResultDTO;
import utc.englishlearning.Encybara.repository.LearningResultRepository;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;
import utc.englishlearning.Encybara.exception.LearningResultException.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearningResultService {

    @Autowired
    private LearningResultRepository learningResultRepository;

    public ResLearningResultDTO getLearningResult(Long userId) {
        Learning_Result result = learningResultRepository.findByUserIdWithHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));
        return mapToResponse(result);
    }

    public ResDetailedLearningResultDTO getDetailedLearningResult(Long userId) {
        Learning_Result result = learningResultRepository.findByUserIdWithHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));
        return mapToDetailedResponse(result);
    }

    public ResLearningResultDTO updateLearningResult(Long userId, ReqUpdateLearningResultDTO request) {
        Learning_Result result = learningResultRepository.findByUserIdWithHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));

        // Validate scores
        validateScore(request.getListeningScore());
        validateScore(request.getSpeakingScore());
        validateScore(request.getReadingScore());
        validateScore(request.getWritingScore());

        // Store previous scores
        result.setPreviousListeningScore(result.getListeningScore());
        result.setPreviousSpeakingScore(result.getSpeakingScore());
        result.setPreviousReadingScore(result.getReadingScore());
        result.setPreviousWritingScore(result.getWritingScore());

        // Update scores
        result.setListeningScore(request.getListeningScore());
        result.setSpeakingScore(request.getSpeakingScore());
        result.setReadingScore(request.getReadingScore());
        result.setWritingScore(request.getWritingScore());
        result.setLastUpdated(Instant.now());

        return mapToResponse(learningResultRepository.save(result));
    }

    private void validateScore(double score) {
        if (score < 1.0 || score > 7.0) {
            throw new InvalidSkillScoreException(score);
        }
    }

    public void evaluateAndUpdateScores(Enrollment enrollment) {
        Learning_Result result = enrollment.getLearningResult();
        double diffLevel = enrollment.getCourse().getDiffLevel();
        double comLevel = enrollment.getComLevel();

        // Calculate evaluation score based on completion level
        double evaluationScore = calculateEvaluationScore(diffLevel, comLevel);

        // Store previous scores before updating
        result.setPreviousListeningScore(result.getListeningScore());
        result.setPreviousSpeakingScore(result.getSpeakingScore());
        result.setPreviousReadingScore(result.getReadingScore());
        result.setPreviousWritingScore(result.getWritingScore());

        // Update relevant skill score based on course type
        switch (enrollment.getCourse().getCourseType()) {
            case LISTENING:
                result.setListeningScore(calculateNewScore(result.getListeningScore(), evaluationScore));
                break;
            case SPEAKING:
                result.setSpeakingScore(calculateNewScore(result.getSpeakingScore(), evaluationScore));
                break;
            case READING:
                result.setReadingScore(calculateNewScore(result.getReadingScore(), evaluationScore));
                break;
            case WRITING:
                result.setWritingScore(calculateNewScore(result.getWritingScore(), evaluationScore));
                break;
            case ALLSKILLS:
                // Update all skills with the evaluation score
                result.setListeningScore(calculateNewScore(result.getListeningScore(), evaluationScore));
                result.setSpeakingScore(calculateNewScore(result.getSpeakingScore(), evaluationScore));
                result.setReadingScore(calculateNewScore(result.getReadingScore(), evaluationScore));
                result.setWritingScore(calculateNewScore(result.getWritingScore(), evaluationScore));
                break;
            default:
                throw new InvalidSkillTypeException(enrollment.getCourse().getCourseType().toString());
        }

        result.setLastUpdated(Instant.now());
        learningResultRepository.save(result);
    }

    public double calculateNewScore(double currentScore, double evaluationScore) {
        // Formula: New Score = (60% current score) + (40% evaluation score) for more
        // significant progress
        return (0.6 * currentScore) + (0.4 * evaluationScore);
    }

    private double calculateEvaluationScore(double diffLevel, double comLevel) {
        // Convert percentage to decimal
        double completionRate = comLevel / 100.0;

        // Calculate score based on completion percentage
        if (completionRate <= 0.50)
            return Math.max(3.0, diffLevel); // At least maintain current level
        if (completionRate <= 0.65)
            return Math.max(4.0, diffLevel + 0.5); // Small improvement
        if (completionRate <= 0.80)
            return Math.max(5.0, diffLevel + 1.0); // Good improvement
        if (completionRate <= 0.90)
            return Math.max(6.0, diffLevel + 1.5); // Significant improvement
        return Math.min(7.0, diffLevel + 2.0); // Maximum improvement
    }

    public ResDetailedLearningResultDTO analyzeRecentProgress(Long userId) {
        Learning_Result result = learningResultRepository.findByUserIdWithRecentHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));

        if (result.getEnrollmentHistory().isEmpty()) {
            throw new InsufficientDataException();
        }

        return mapToDetailedResponse(result);
    }

    public double[] getCompletionRatesByDifficulty(Long userId) {
        Learning_Result result = learningResultRepository.findByUserIdWithHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));

        if (result.getEnrollmentHistory().isEmpty()) {
            throw new InsufficientDataException();
        }

        List<Object[]> rates = learningResultRepository.findCompletionRatesByDifficulty(result.getId());
        return rates.stream()
                .mapToDouble(row -> (Double) row[1])
                .toArray();
    }

    public double getRecommendedLevel(Long userId, SkillTypeEnum skillType) {
        if (skillType == null) {
            throw new InvalidSkillTypeException("null");
        }

        Learning_Result result = learningResultRepository.findByUserIdWithHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));

        // Get current skill level
        double currentLevel = switch (skillType) {
            case LISTENING -> result.getListeningScore();
            case SPEAKING -> result.getSpeakingScore();
            case READING -> result.getReadingScore();
            case WRITING -> result.getWritingScore();
            case ALLSKILLS -> (result.getListeningScore() + result.getSpeakingScore() +
                    result.getReadingScore() + result.getWritingScore()) / 4.0;
            default -> throw new InvalidSkillTypeException(skillType.toString());
        };

        // Round to nearest 0.5
        return Math.round(currentLevel * 2) / 2.0;
    }

    public boolean isReadyForHigherLevel(Long userId, SkillTypeEnum skillType) {
        if (skillType == null) {
            throw new InvalidSkillTypeException("null");
        }

        Learning_Result result = learningResultRepository.findByUserIdWithRecentHistory(userId)
                .orElseThrow(() -> new LearningResultNotFoundException(userId));

        if (result.getEnrollmentHistory().isEmpty()) {
            throw new InsufficientDataException();
        }

        // Check recent completion rates
        long highCompletionCount = result.getEnrollmentHistory().stream()
                .filter(e -> e.getComLevel() >= 0.8)
                .count();

        // If at least 3 recent courses have high completion, suggest level increase
        return highCompletionCount >= 3;
    }

    private ResLearningResultDTO mapToResponse(Learning_Result result) {
        ResLearningResultDTO response = new ResLearningResultDTO();
        response.setId(result.getId());
        response.setListeningScore(result.getListeningScore());
        response.setSpeakingScore(result.getSpeakingScore());
        response.setReadingScore(result.getReadingScore());
        response.setWritingScore(result.getWritingScore());
        response.setLastUpdated(result.getLastUpdated());

        // Set previous scores
        response.setPreviousListeningScore(result.getPreviousListeningScore());
        response.setPreviousSpeakingScore(result.getPreviousSpeakingScore());
        response.setPreviousReadingScore(result.getPreviousReadingScore());
        response.setPreviousWritingScore(result.getPreviousWritingScore());

        // Calculate progress
        response.setListeningProgress(result.getListeningScore() - result.getPreviousListeningScore());
        response.setSpeakingProgress(result.getSpeakingScore() - result.getPreviousSpeakingScore());
        response.setReadingProgress(result.getReadingScore() - result.getPreviousReadingScore());
        response.setWritingProgress(result.getWritingScore() - result.getPreviousWritingScore());

        // Calculate overall score
        response.setOverallScore((result.getListeningScore() + result.getSpeakingScore() +
                result.getReadingScore() + result.getWritingScore()) / 4.0);

        return response;
    }

    private ResDetailedLearningResultDTO mapToDetailedResponse(Learning_Result result) {
        ResDetailedLearningResultDTO response = new ResDetailedLearningResultDTO();
        // Copy base response data
        ResLearningResultDTO baseResponse = mapToResponse(result);
        response.setId(baseResponse.getId());
        response.setListeningScore(baseResponse.getListeningScore());
        response.setSpeakingScore(baseResponse.getSpeakingScore());
        response.setReadingScore(baseResponse.getReadingScore());
        response.setWritingScore(baseResponse.getWritingScore());
        response.setLastUpdated(baseResponse.getLastUpdated());
        response.setPreviousListeningScore(baseResponse.getPreviousListeningScore());
        response.setPreviousSpeakingScore(baseResponse.getPreviousSpeakingScore());
        response.setPreviousReadingScore(baseResponse.getPreviousReadingScore());
        response.setPreviousWritingScore(baseResponse.getPreviousWritingScore());
        response.setListeningProgress(baseResponse.getListeningProgress());
        response.setSpeakingProgress(baseResponse.getSpeakingProgress());
        response.setReadingProgress(baseResponse.getReadingProgress());
        response.setWritingProgress(baseResponse.getWritingProgress());
        response.setOverallScore(baseResponse.getOverallScore());

        // Add enrollment history
        response.setEnrollmentHistory(result.getEnrollmentHistory().stream()
                .map(this::mapToEnrollmentHistoryItem)
                .collect(Collectors.toList()));

        return response;
    }

    private ResDetailedLearningResultDTO.EnrollmentHistoryItem mapToEnrollmentHistoryItem(Enrollment enrollment) {
        ResDetailedLearningResultDTO.EnrollmentHistoryItem item = new ResDetailedLearningResultDTO.EnrollmentHistoryItem();
        item.setEnrollmentId(enrollment.getId());
        item.setCourseId(enrollment.getCourse().getId());
        item.setCourseName(enrollment.getCourse().getName());
        item.setDiffLevel(enrollment.getCourse().getDiffLevel());
        item.setComLevel(enrollment.getComLevel());
        item.setSkillScore(enrollment.getSkillScore());
        item.setEnrollDate(enrollment.getEnrollDate());
        return item;
    }
}