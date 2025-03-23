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
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexitySuggestionResponse;
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
                            Score: (number from 0-10 based on accuracy and completeness)
                            Evaluation: (brief evaluation of the answer's strengths and weaknesses in Vietnamese)
                            Improvements: (provide a single English sentence that answers the question with a good score)

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
            boolean isReadingEvaluation = false;
            boolean isReadingImprovements = false;

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                // Handle both regular "Score:" and markdown "## Score:" formats
                if (line.matches("(?:##\\s*)?Score:.*")) {
                    String scoreText = line.replaceAll("(?:##\\s*)?Score:\\s*", "").trim();
                    try {
                        score = Double.parseDouble(scoreText.replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse score from: {}", scoreText);
                    }
                    isReadingEvaluation = false;
                    isReadingImprovements = false;
                }
                // Handle both regular "Evaluation:" and markdown "## Evaluation:" formats
                else if (line.matches("(?:##\\s*)?Evaluation:.*")) {
                    evaluation = line.replaceAll("(?:##\\s*)?Evaluation:\\s*", "").trim();
                    isReadingEvaluation = true;
                    isReadingImprovements = false;
                }
                // Handle both regular "Improvements:" and markdown "## Improvements:" formats
                else if (line.matches("(?:##\\s*)?Improvements:.*")) {
                    improvements.setLength(0);
                    improvements.append(line.replaceAll("(?:##\\s*)?Improvements:\\s*", "").trim());
                    isReadingEvaluation = false;
                    isReadingImprovements = true;
                } else if (isReadingEvaluation) {
                    // If the next line starts with ##, it's a new section
                    if (line.startsWith("##")) {
                        isReadingEvaluation = false;
                    } else {
                        evaluation += " " + line.trim();
                    }
                } else if (isReadingImprovements) {
                    // If the next line starts with ##, it's a new section
                    if (line.startsWith("##")) {
                        isReadingImprovements = false;
                    } else {
                        improvements.append(" ").append(line.trim());
                    }
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

    public PerplexitySuggestionResponse getSuggestions(String question, String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "sonar");
            body.put("temperature", 0.7);
            body.put("max_tokens", 300);

            String promptContent = String.format(
                    """
                            As an English teacher, help a student understand how to answer this question.
                            Follow this exact format without any deviations:

                            Question: %s
                            Context: %s

                            Key Points:
                            key point in English

                            Sample Structure:
                            Sample answer in English

                            Tips:
                            1. First tip in English
                            2. Second tip in English
                            3. Third tip in English

                            Rules:
                            1. Keep each section's format exactly as shown above
                            2. Use simple numbered lists (1., 2., 3.)
                            3. No special formatting or symbols (bold, italics, etc.)""",
                    question, prompt);

            body.put("messages", List.of(
                    Map.of("role", "user", "content", promptContent)));

            var response = restTemplate.postForEntity(
                    API_URL,
                    new org.springframework.http.HttpEntity<>(body, headers),
                    Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                String errorMessage = String.format("API request failed. Status: %s, Body: %s",
                        response.getStatusCode(), response.getBody());
                log.error(errorMessage);
                throw new PerplexityException(errorMessage, HttpStatus.SERVICE_UNAVAILABLE.value());
            }

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

            return parseSuggestionResponse(content);

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
            log.error("Failed to get suggestions: {}", e.getMessage());
            throw new PerplexityException(
                    "Failed to get suggestions: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private PerplexitySuggestionResponse parseSuggestionResponse(String content) {
        try {
            log.debug("Parsing suggestion response content: {}", content);
            // Split content into sections using section headers
            String[] sections = content.split("(?=Key Points:|Sample Structure:|Tips:)");

            // Initialize section content
            String keyPointsStr = "";
            String sampleStructureStr = "";
            String tipsStr = "";

            // Process each section
            for (String section : sections) {
                section = section.trim();
                if (section.isEmpty() || section.startsWith("Question:") || section.startsWith("Context:") ||
                        section.startsWith("IMPORTANT:")) {
                    continue;
                }

                // Extract and clean section content
                String cleanContent = section.replaceAll("\\s+", " ").trim();

                if (section.startsWith("Key Points:")) {
                    keyPointsStr = cleanContent.substring("Key Points:".length()).trim();
                } else if (section.startsWith("Sample Structure:")) {
                    sampleStructureStr = cleanContent.substring("Sample Structure:".length()).trim();
                } else if (section.startsWith("Tips:")) {
                    tipsStr = cleanContent.substring("Tips:".length()).trim();
                }
            }

            // Log raw content for debugging
            log.debug("Raw sections - KeyPoints: {}", keyPointsStr);
            log.debug("Raw sections - SampleStructure: {}", sampleStructureStr);
            log.debug("Raw sections - Tips: {}", tipsStr);

            // Ensure we have at least some content
            if (keyPointsStr.isEmpty() && sampleStructureStr.isEmpty() && tipsStr.isEmpty()) {
                log.error("No valid content found in response: {}", content);
                throw new PerplexityException("Failed to parse response: no valid content found",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            // Use empty placeholder if section is missing
            if (keyPointsStr.isEmpty()) {
                keyPointsStr = "No key points provided";
                log.warn("Key points section missing in response");
            }
            if (sampleStructureStr.isEmpty()) {
                sampleStructureStr = "No sample structure provided";
                log.warn("Sample structure section missing in response");
            }
            if (tipsStr.isEmpty()) {
                tipsStr = "No tips provided";
                log.warn("Tips section missing in response");
            }

            // Log the parsed sections for debugging
            log.debug("Parsed sections - KeyPoints: {} chars, SampleStructure: {} chars, Tips: {} chars",
                    keyPointsStr.length(), sampleStructureStr.length(), tipsStr.length());

            return PerplexitySuggestionResponse.builder()
                    .keyPoints(keyPointsStr)
                    .sampleAnswer(sampleStructureStr)
                    .tips(tipsStr)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse suggestion response: {}", e.getMessage());
            log.error("Response content was: {}", content);
            throw new PerplexityException("Failed to parse API response: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
