package utc.englishlearning.Encybara.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import utc.englishlearning.Encybara.exception.PronunciationAssessmentException;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class PronunciationAssessmentService {

    private final RestTemplate restTemplate;

    @Value("${pronunciation-assessment.service.url:}")
    private String pronunciationServiceUrl;

    public PronunciationAssessmentService(RestTemplateBuilder restTemplateBuilder) {
        // Configure RestTemplate with 10 second timeout
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Assess pronunciation using pronunciation-assessment-service
     * 
     * @param audioFile The audio file to assess
     * @param text      Optional text reference (can be null)
     * @return Assessment result
     */
    public Map<String, Object> assessPronunciation(MultipartFile audioFile, String text) {
        try {
            if (pronunciationServiceUrl == null || pronunciationServiceUrl.trim().isEmpty()) {
                throw new PronunciationAssessmentException(
                        "Pronunciation assessment service chưa được cấu hình. Vui lòng kiểm tra biến môi trường PRONUNCIATION_SERVICE_URL.",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            log.info("Assessing pronunciation via pronunciation-assessment-service for file: {}",
                    audioFile.getOriginalFilename());

            // Prepare multipart request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", audioFile.getResource());

            // pronunciation-assessment-service requires 'transcript' field, not 'text'
            if (text != null && !text.trim().isEmpty()) {
                body.add("transcript", text);
            } else {
                // transcript is required by pronunciation-assessment-service
                throw new PronunciationAssessmentException(
                        "Transcript text là bắt buộc để đánh giá phát âm. Vui lòng cung cấp text tham chiếu.",
                        HttpStatus.BAD_REQUEST.value());
            }

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Make API call to pronunciation-assessment-service - correct endpoint
            String apiUrl = pronunciationServiceUrl + "/api/pronunciation-assessment";
            log.debug("Calling pronunciation-assessment-service at: {}", apiUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl,
                    new org.springframework.http.HttpEntity<>(body, headers),
                    Map.class);

            log.debug("Pronunciation-assessment-service response status: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                String errorMessage = String.format(
                        "Pronunciation-assessment-service request failed. Status: %s, Body: %s",
                        response.getStatusCode(), response.getBody());
                log.error(errorMessage);
                throw new PronunciationAssessmentException(
                        "Pronunciation-assessment-service không phản hồi đúng cách. Vui lòng thử lại sau.",
                        HttpStatus.SERVICE_UNAVAILABLE.value());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            log.info("Successfully received pronunciation assessment result");
            return responseBody;

        } catch (ResourceAccessException e) {
            log.error("Timeout or connection error to pronunciation-assessment-service: {}", e.getMessage());
            throw new PronunciationAssessmentException(
                    "Không thể kết nối tới pronunciation-assessment-service. Vui lòng kiểm tra service có đang chạy không (timeout 10s).",
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        } catch (HttpClientErrorException e) {
            log.error("Pronunciation-assessment-service HTTP error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            String userMessage;
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                userMessage = "Pronunciation-assessment-service endpoint không tìm thấy. Vui lòng kiểm tra cấu hình service.";
            } else if (e.getStatusCode().is4xxClientError()) {
                userMessage = "Dữ liệu gửi tới pronunciation-assessment-service không hợp lệ. Vui lòng kiểm tra lại file audio.";
            } else {
                userMessage = "Pronunciation-assessment-service gặp lỗi. Vui lòng thử lại sau.";
            }

            throw new PronunciationAssessmentException(userMessage, e.getStatusCode().value());
        } catch (RestClientException e) {
            log.error("REST client error when calling pronunciation-assessment-service: {}", e.getMessage());
            throw new PronunciationAssessmentException(
                    "Lỗi kết nối tới pronunciation-assessment-service: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.value());
        } catch (PronunciationAssessmentException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when calling pronunciation-assessment-service: {}", e.getMessage(), e);
            throw new PronunciationAssessmentException(
                    "Lỗi không mong muốn khi đánh giá phát âm: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Check if pronunciation-assessment-service is available
     * 
     * @return true if service is healthy, false otherwise
     */
    public boolean isServiceAvailable() {
        try {
            if (pronunciationServiceUrl == null || pronunciationServiceUrl.trim().isEmpty()) {
                return false;
            }
            String healthUrl = pronunciationServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Pronunciation-assessment-service health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get service URL for debugging
     */
    public String getServiceUrl() {
        return pronunciationServiceUrl;
    }
}