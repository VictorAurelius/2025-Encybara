package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.learning_result.ReqUpdateLearningResultDTO;
import utc.englishlearning.Encybara.domain.response.learning_result.ResLearningResultDTO;
import utc.englishlearning.Encybara.domain.response.learning_result.ResDetailedLearningResultDTO;
import utc.englishlearning.Encybara.service.LearningResultService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/learning-results")
public class LearningResultController {

    @Autowired
    private LearningResultService learningResultService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<ResLearningResultDTO>> getLearningResult(
            @PathVariable("userId") Long userId) {
        ResLearningResultDTO result = learningResultService.getLearningResult(userId);
        RestResponse<ResLearningResultDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning result retrieved successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/detailed")
    public ResponseEntity<RestResponse<ResDetailedLearningResultDTO>> getDetailedLearningResult(
            @PathVariable("userId") Long userId) {
        ResDetailedLearningResultDTO result = learningResultService.getDetailedLearningResult(userId);
        RestResponse<ResDetailedLearningResultDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Detailed learning result retrieved successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<RestResponse<ResDetailedLearningResultDTO>> analyzeProgress(
            @PathVariable("userId") Long userId) {
        ResDetailedLearningResultDTO result = learningResultService.analyzeRecentProgress(userId);
        RestResponse<ResDetailedLearningResultDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning progress analyzed successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/completion-rates")
    public ResponseEntity<RestResponse<double[]>> getCompletionRates(
            @PathVariable("userId") Long userId) {
        double[] rates = learningResultService.getCompletionRatesByDifficulty(userId);
        RestResponse<double[]> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Completion rates retrieved successfully");
        response.setData(rates);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/recommended-level")
    public ResponseEntity<RestResponse<Double>> getRecommendedLevel(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "skillType", required = false) SkillTypeEnum skillType) {
        double level = learningResultService.getRecommendedLevel(userId, skillType);
        RestResponse<Double> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Recommended level retrieved successfully");
        response.setData(level);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/ready-for-upgrade")
    public ResponseEntity<RestResponse<Boolean>> isReadyForHigherLevel(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "skillType", required = false) SkillTypeEnum skillType) {
        boolean ready = learningResultService.isReadyForHigherLevel(userId, skillType);
        RestResponse<Boolean> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Level upgrade readiness checked successfully");
        response.setData(ready);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<RestResponse<ResLearningResultDTO>> updateLearningResult(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody ReqUpdateLearningResultDTO request) {
        ResLearningResultDTO result = learningResultService.updateLearningResult(userId, request);
        RestResponse<ResLearningResultDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning result updated successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }
}