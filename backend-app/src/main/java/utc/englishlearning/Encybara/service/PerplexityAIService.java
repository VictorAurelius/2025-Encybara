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
            body.put("model", "pplx-7b-chat");
            body.put("temperature", 0.7);
            body.put("max_tokens", 300);

            String promptContent = String.format("""
                    Act as an English teacher. Evaluate this answer using exactly this format:
                    
                    Question: %s
                    Student's Answer: %s
                    Context: %s
                    
                    Your response must follow this exact structure:
                    Score: [0-100]
                    Evaluation: [concise evaluation]
                    Improvements: [specific suggestions]""",
                    question, userAnswer, prompt);

            body.put("messages", List.of(
                    Map.of("role", "user", "content", promptContent)));

            // Make API call and log response
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

            // Parse response
            @SuppressWarnings("unchecked")
            var choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new PerplexityException("No response content from API",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
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
            String[] parts = content.split("\n");
            double score = 0;
            String evaluation = "";
            String improvements = "";

            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("Score:")) {
                    score = Double.parseDouble(part.substring(6).trim().replaceAll("[^0-9.]", ""));
                } else if (part.startsWith("Evaluation:")) {
                    evaluation = part.substring(11).trim();
                } else if (part.startsWith("Improvements:")) {
                    improvements = part.substring(13).trim();
                }
            }

            return PerplexityResponse.builder()
                    .score(score)
                    .evaluation(evaluation)
                    .improvements(improvements)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse response: {}", e.getMessage());
            log.error("Response content was: {}", content);
            throw new PerplexityException("Failed to parse API response: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}