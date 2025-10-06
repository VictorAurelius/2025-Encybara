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
import utc.englishlearning.Encybara.domain.response.scoring.ScoringResponse;
import utc.englishlearning.Encybara.exception.ContentScoringException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ContentScoringService {

    private final RestTemplate restTemplate;

    @Value("${content-scoring.service.url:http://localhost:5001}")
    private String contentScoringServiceUrl;

    @Value("${content-scoring.service.timeout.connect:10}")
    private int connectTimeout;

    @Value("${content-scoring.service.timeout.read:10}")
    private int readTimeout;

    public ContentScoringService(RestTemplateBuilder restTemplateBuilder,
                                @Value("${content-scoring.service.timeout.connect:10}") int connectTimeout,
                                @Value("${content-scoring.service.timeout.read:10}") int readTimeout) {
        // Configure RestTemplate with timeout from application.properties
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(connectTimeout))
                .setReadTimeout(Duration.ofSeconds(readTimeout))
                .build();
    }

    /**
     * Evaluate answer using content-scoring-service
     * 
     * @param question   The question to evaluate
     * @param userAnswer The user's answer
     * @param prompt     Optional context/prompt (can be null)
     * @return ScoringResponse containing score (thang 10) and improvements
     */
    public ScoringResponse evaluateAnswer(String question, String userAnswer, String prompt) {
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
                    String.format("Không thể kết nối tới content-scoring-service. Vui lòng kiểm tra service có đang chạy không (timeout %ds).", readTimeout),
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
     * Expected format:
     * {
     * "success": true,
     * "score": 85.5, // thang 100
     * "similarity": 0.855,
     * "key_points": [...],
     * "advanced_answer": {
     * "suggestion": "...",
     * "improvement_points": [...],
     * "missing_concepts": [...]
     * }
     * }
     */
    private ScoringResponse parseContentScoringResponse(Map<String, Object> responseBody) {
        try {
            log.debug("Parsing content-scoring-service response: {}", responseBody);

            // Extract score (thang 100) và convert sang thang 10
            Object scoreObj = responseBody.get("score");
            double scoreIn100 = 0.0;
            if (scoreObj instanceof Number) {
                scoreIn100 = ((Number) scoreObj).doubleValue();
            } else if (scoreObj instanceof String) {
                try {
                    scoreIn100 = Double.parseDouble((String) scoreObj);
                } catch (NumberFormatException e) {
                    log.warn("Could not parse score from string: {}", scoreObj);
                }
            }

            // Convert từ thang 100 sang thang 10
            double scoreIn10 = scoreIn100 / 10.0;

            // Validate score range
            if (scoreIn10 < 0 || scoreIn10 > 10) {
                log.warn("Score out of range (0-10): {}. Clamping to valid range.", scoreIn10);
                scoreIn10 = Math.max(0, Math.min(10, scoreIn10));
            }

            // Extract improvements từ advanced_answer
            String improvements = extractImprovements(responseBody);

            log.info(
                    "Successfully parsed content-scoring-service response - Score: {}/100 -> {}/10, Improvements length: {}",
                    scoreIn100, scoreIn10, improvements.length());

            return ScoringResponse.builder()
                    .score(scoreIn10)
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
     * Extract improvements từ advanced_answer section
     */
    private String extractImprovements(Map<String, Object> responseBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> advancedAnswer = (Map<String, Object>) responseBody.get("advanced_answer");

            if (advancedAnswer == null) {
                log.warn("No advanced_answer found in response, using fallback");
                return "Không có gợi ý cải thiện chi tiết từ content-scoring-service.";
            }

            StringBuilder improvements = new StringBuilder();

            // Extract suggestion
            String suggestion = (String) advancedAnswer.get("suggestion");
            if (suggestion != null && !suggestion.trim().isEmpty()) {
                improvements.append("Suggestions: ").append(suggestion).append("\n\n");
            }

            // Extract improvement_points
            @SuppressWarnings("unchecked")
            List<String> improvementPoints = (List<String>) advancedAnswer.get("improvement_points");
            if (improvementPoints != null && !improvementPoints.isEmpty()) {
                improvements.append("Improvement Points:\n");
                for (int i = 0; i < improvementPoints.size(); i++) {
                    improvements.append(String.format("• %s\n", improvementPoints.get(i)));
                }
                improvements.append("\n");
            }

            // Extract missing_concepts
            @SuppressWarnings("unchecked")
            List<String> missingConcepts = (List<String>) advancedAnswer.get("missing_concepts");
            if (missingConcepts != null && !missingConcepts.isEmpty()) {
                improvements.append("Missing Concepts: ");
                improvements.append(String.join(", ", missingConcepts));
            }

            String result = improvements.toString().trim();
            return result.isEmpty() ? "Không có gợi ý cải thiện cụ thể từ content-scoring-service." : result;

        } catch (Exception e) {
            log.warn("Error extracting improvements from advanced_answer: {}", e.getMessage());
            return "Không thể trích xuất gợi ý cải thiện từ phản hồi của content-scoring-service.";
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