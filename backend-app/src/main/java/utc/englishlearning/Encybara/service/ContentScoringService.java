package utc.englishlearning.Encybara.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityResponse;
import utc.englishlearning.Encybara.exception.ContentScoringException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ContentScoringService {

    private final RestTemplate restTemplate;

    @Value("${content-scoring.service.url:http://localhost:5001}")
    private String contentScoringServiceUrl;

    public ContentScoringService(RestTemplateBuilder restTemplateBuilder) {
        // Configure RestTemplate with 10 second timeout
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Evaluate answer using content-scoring-service
     * 
     * @param question   The question to evaluate
     * @param userAnswer The user's answer
     * @param prompt     Optional context/prompt (can be null)
     * @return PerplexityResponse containing score, evaluation, and improvements
     */
    public PerplexityResponse evaluateAnswer(String question, String userAnswer, String prompt) {
        try {
            log.info("Evaluating answer via content-scoring-service for question: {}",
                    question.length() > 50 ? question.substring(0, 50) + "..." : question);

            // Prepare request body for content-scoring-service
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("question", question);
            requestBody.put("answer", userAnswer);

            // Add prompt if provided
            if (prompt != null && !prompt.trim().isEmpty()) {
                requestBody.put("context", prompt);
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make API call to content-scoring-service
            String apiUrl = contentScoringServiceUrl + "/api/content-scoring";
            log.debug("Calling content-scoring-service at: {}", apiUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl,
                    new org.springframework.http.HttpEntity<>(requestBody, headers),
                    Map.class);

            log.debug("Content-scoring-service response status: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                String errorMessage = String.format(
                        "Content-scoring-service request failed. Status: %s, Body: %s",
                        response.getStatusCode(), response.getBody());
                log.error(errorMessage);
                throw new ContentScoringException(
                        "Content-scoring-service không phản hồi đúng cách. Vui lòng thử lại sau.",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            // Parse response from content-scoring-service
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            return parseContentScoringResponse(responseBody);

        } catch (ResourceAccessException e) {
            log.error("Timeout or connection error to content-scoring-service: {}", e.getMessage());
            throw new ContentScoringException(
                    "Không thể kết nối tới content-scoring-service. Vui lòng kiểm tra service có đang chạy không (timeout 10s).",
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        } catch (HttpClientErrorException e) {
            log.error("Content-scoring-service HTTP error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            String userMessage;
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                userMessage = "Content-scoring-service endpoint không tìm thấy. Vui lòng kiểm tra cấu hình service.";
            } else if (e.getStatusCode().is4xxClientError()) {
                userMessage = "Dữ liệu gửi tới content-scoring-service không hợp lệ. Vui lòng kiểm tra lại câu hỏi và câu trả lời.";
            } else {
                userMessage = "Content-scoring-service gặp lỗi. Vui lòng thử lại sau.";
            }

            throw new ContentScoringException(userMessage, e.getStatusCode().value());
        } catch (RestClientException e) {
            log.error("REST client error when calling content-scoring-service: {}", e.getMessage());
            throw new ContentScoringException(
                    "Lỗi kết nối tới content-scoring-service: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        } catch (ContentScoringException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when calling content-scoring-service: {}", e.getMessage(), e);
            throw new ContentScoringException(
                    "Lỗi không mong muốn khi đánh giá câu trả lời: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Parse response from content-scoring-service API
     * Expected format: {"score": 8.5, "feedback": "Good answer but...",
     * "suggestions": "Try to..."}
     */
    private PerplexityResponse parseContentScoringResponse(Map<String, Object> responseBody) {
        try {
            log.debug("Parsing content-scoring-service response: {}", responseBody);

            // Extract score
            Object scoreObj = responseBody.get("score");
            double score = 0.0;
            if (scoreObj instanceof Number) {
                score = ((Number) scoreObj).doubleValue();
            } else if (scoreObj instanceof String) {
                try {
                    score = Double.parseDouble((String) scoreObj);
                } catch (NumberFormatException e) {
                    log.warn("Could not parse score from string: {}", scoreObj);
                }
            }

            // Extract feedback (evaluation)
            String evaluation = (String) responseBody.get("feedback");
            if (evaluation == null) {
                evaluation = (String) responseBody.get("evaluation");
            }
            if (evaluation == null || evaluation.trim().isEmpty()) {
                evaluation = "Không có đánh giá chi tiết từ content-scoring-service.";
            }

            // Extract suggestions (improvements)
            String improvements = (String) responseBody.get("suggestions");
            if (improvements == null) {
                improvements = (String) responseBody.get("improvements");
            }
            if (improvements == null || improvements.trim().isEmpty()) {
                improvements = "Không có gợi ý cải thiện từ content-scoring-service.";
            }

            // Validate score range
            if (score < 0 || score > 10) {
                log.warn("Score out of range (0-10): {}. Clamping to valid range.", score);
                score = Math.max(0, Math.min(10, score));
            }

            log.info(
                    "Successfully parsed content-scoring-service response - Score: {}, Evaluation length: {}, Improvements length: {}",
                    score, evaluation.length(), improvements.length());

            return PerplexityResponse.builder()
                    .score(score)
                    .evaluation(evaluation)
                    .improvements(improvements)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse content-scoring-service response: {}", e.getMessage());
            log.error("Response was: {}", responseBody);
            throw new ContentScoringException(
                    "Không thể phân tích phản hồi từ content-scoring-service: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Check if content-scoring-service is available
     * 
     * @return true if service is healthy, false otherwise
     */
    public boolean isServiceAvailable() {
        try {
            String healthUrl = contentScoringServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Content-scoring-service health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get service URL for debugging
     */
    public String getServiceUrl() {
        return contentScoringServiceUrl;
    }
}