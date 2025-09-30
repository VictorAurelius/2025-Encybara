package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.perplexity.PerplexityRequest;
import utc.englishlearning.Encybara.domain.request.perplexity.PerplexitySuggestionRequest;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityResponse;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexitySuggestionResponse;
import utc.englishlearning.Encybara.exception.ContentScoringException;
import utc.englishlearning.Encybara.service.ContentScoringService;

@RestController
@RequestMapping("/api/v1/content-scoring")
@RequiredArgsConstructor
@Slf4j
public class ContentScoringController {

    private final ContentScoringService contentScoringService;

    @PostMapping("/evaluate")
    public ResponseEntity<RestResponse<PerplexityResponse>> evaluateAnswer(
            @RequestBody PerplexityRequest request) {

        if (request.getUserAnswer() == null || request.getQuestion() == null) {
            throw new ContentScoringException("User answer and question are required",
                    HttpStatus.BAD_REQUEST.value());
        }

        if (request.getUserAnswer().trim().isEmpty() || request.getQuestion().trim().isEmpty()) {
            throw new ContentScoringException("User answer and question cannot be empty",
                    HttpStatus.BAD_REQUEST.value());
        }

        try {
            log.info("Evaluating answer via content-scoring-service for question length: {} chars",
                    request.getQuestion().length());

            PerplexityResponse evalResponse = contentScoringService.evaluateAnswer(
                    request.getQuestion(),
                    request.getUserAnswer(),
                    request.getPrompt());

            RestResponse<PerplexityResponse> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Answer evaluated successfully via content-scoring-service");
            response.setData(evalResponse);

            log.info("Successfully evaluated answer with score: {}", evalResponse.getScore());
            return ResponseEntity.ok(response);

        } catch (ContentScoringException e) {
            log.error("Content scoring error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during evaluation: {}", e.getMessage(), e);
            throw new ContentScoringException(
                    "Đã xảy ra lỗi không mong muốn khi đánh giá câu trả lời. Vui lòng thử lại.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @PostMapping("/suggest")
    public ResponseEntity<RestResponse<PerplexitySuggestionResponse>> getSuggestions(
            @RequestBody PerplexitySuggestionRequest request) {

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new ContentScoringException("Question is required",
                    HttpStatus.BAD_REQUEST.value());
        }

        log.warn(
                "Suggestions endpoint called but temporarily disabled - content-scoring-service doesn't support suggestions yet");

        RestResponse<PerplexitySuggestionResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setMessage(
                "Tính năng gợi ý tạm thời không khả dụng. Content-scoring-service chưa hỗ trợ endpoint /suggest.");

        PerplexitySuggestionResponse suggestionResponse = PerplexitySuggestionResponse.builder()
                .keyPoints(
                        "Tính năng gợi ý đang được phát triển và sẽ có trong phiên bản tiếp theo của content-scoring-service.")
                .sampleAnswer("Hiện tại chỉ có chức năng đánh giá câu trả lời. Vui lòng sử dụng endpoint /evaluate.")
                .tips("1. Hãy thử endpoint /evaluate để đánh giá câu trả lời\n2. Tính năng suggestions sẽ được bổ sung sau\n3. Liên hệ admin để biết thêm chi tiết")
                .build();

        response.setData(suggestionResponse);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<RestResponse<String>> healthCheck() {
        try {
            boolean isServiceAvailable = contentScoringService.isServiceAvailable();
            String serviceUrl = contentScoringService.getServiceUrl();

            RestResponse<String> response = new RestResponse<>();

            if (isServiceAvailable) {
                response.setStatusCode(HttpStatus.OK.value());
                response.setMessage("Content-scoring-service is available");
                response.setData("Service URL: " + serviceUrl + " - Status: HEALTHY");
                return ResponseEntity.ok(response);
            } else {
                response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                response.setMessage("Content-scoring-service is not available");
                response.setData("Service URL: " + serviceUrl + " - Status: UNAVAILABLE");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Health check failed: " + e.getMessage());
            response.setData("Error checking content-scoring-service");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<RestResponse<Object>> getServiceInfo() {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Content-scoring-service integration info");

        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("service_type", "content-scoring-service");
        info.put("service_url", contentScoringService.getServiceUrl());
        info.put("timeout", "10 seconds");
        info.put("available_endpoints", new String[] { "/evaluate" });
        info.put("disabled_endpoints", new String[] { "/suggest (temporarily)" });
        info.put("version", "1.0");
        info.put("description", "Integration with Python content-scoring-service for ML-based answer evaluation");

        response.setData(info);
        return ResponseEntity.ok(response);
    }
}