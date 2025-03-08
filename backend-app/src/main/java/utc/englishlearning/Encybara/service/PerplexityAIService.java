package utc.englishlearning.Encybara.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityResponse;
import utc.englishlearning.Encybara.exception.PerplexityException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PerplexityAIService {

    private static final String API_URL = "https://api.perplexity.ai/chat/completions";
    private final RestTemplate restTemplate;

    @Value("${perplexity.api.key}")
    private String apiKey;

    public PerplexityAIService() {
        this.restTemplate = new RestTemplate();
    }

    public PerplexityResponse evaluateAnswer(String question, String userAnswer, String prompt) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // Prepare request body
            Map<String, Object> body = new HashMap<>();
            body.put("model", "sonar");
            body.put("temperature", 0.7);
            body.put("max_tokens", 300);

            String promptContent = String.format(
                    """
                            You are an English teacher evaluating a student's answer. You must provide feedback using exactly this format:

                            Question: %s
                            Student's Answer: %s
                            Context: %s

                            Respond with these exact sections:
                            Score: (number from 0-100 based on accuracy and completeness)
                            Evaluation: (brief evaluation of the answer's strengths and weaknesses)
                            Improvements: (provide 2-3 specific suggestions for improving the answer)

                            IMPORTANT: Your response MUST include all three sections with detailed improvements.""",
                    question, userAnswer, prompt);

            body.put("messages", List.of(
                    Map.of("role", "user", "content", promptContent)));

            // Make API call
            var response = restTemplate.postForEntity(
                    API_URL,
                    new org.springframework.http.HttpEntity<>(body, headers),
                    Map.class);

            log.debug("API Response status: {}", response.getStatusCode());
            log.debug("API Response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                String errorMessage = String.format("API request failed. Status: %s, Body: %s",
                        response.getStatusCode(), response.getBody());
                log.error(errorMessage);
                throw new PerplexityException(errorMessage, HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            // Parse response with proper type safety
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

            if (choices == null || choices.isEmpty()) {
                throw new PerplexityException("No response content from API",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            if (message == null) {
                throw new PerplexityException("Invalid response format from API",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            String content = (String) message.get("content");
            if (content == null || content.isEmpty()) {
                throw new PerplexityException("Empty response content from API",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            return parseResponse(content);

        } catch (HttpClientErrorException e) {
            log.error("API Error Response: {}", e.getResponseBodyAsString());
            throw new PerplexityException(
                    String.format("API request failed: %s", e.getResponseBodyAsString()),
                    e.getStatusCode().value());
        } catch (RestClientException e) {
            log.error("REST client error: {}", e.getMessage());
            throw new PerplexityException(
                    "Failed to communicate with Perplexity API: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        } catch (Exception e) {
            log.error("Failed to evaluate answer: {}", e.getMessage());
            throw new PerplexityException(
                    "Failed to evaluate answer: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private PerplexityResponse parseResponse(String content) {
        try {
            log.debug("Parsing response content: {}", content);
            String[] lines = content.split("\n");
            double score = 0;
            String evaluation = "";
            StringBuilder improvements = new StringBuilder();
            boolean isReadingImprovements = false;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                if (line.startsWith("Score:")) {
                    score = Double.parseDouble(line.substring(6).trim().replaceAll("[^0-9.]", ""));
                    isReadingImprovements = false;
                } else if (line.startsWith("Evaluation:")) {
                    evaluation = line.substring(11).trim();
                    isReadingImprovements = false;
                } else if (line.startsWith("Improvements:")) {
                    improvements.setLength(0);
                    improvements.append(line.substring(13).trim());
                    isReadingImprovements = true;
                } else if (isReadingImprovements) {
                    improvements.append(" ").append(line.trim());
                }
            }

            String improvementsStr = improvements.toString().trim();

            // Validate all sections are present
            if (score == 0) {
                log.error("Invalid or missing score in response: {}", content);
                throw new PerplexityException("Failed to parse score from response",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            if (evaluation.isEmpty()) {
                log.error("Missing evaluation in response: {}", content);
                throw new PerplexityException("Failed to parse evaluation from response",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            if (improvementsStr.isEmpty()) {
                log.warn("No improvements found in response: {}", content);
                improvementsStr = "No specific improvements provided";
            }

            log.debug("Parsed response - Score: {}, Evaluation length: {}, Improvements length: {}",
                    score, evaluation.length(), improvementsStr.length());

            return PerplexityResponse.builder()
                    .score(score)
                    .evaluation(evaluation)
                    .improvements(improvementsStr)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse response: {}", e.getMessage());
            log.error("Response content was: {}", content);
            throw new PerplexityException("Failed to parse API response: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
