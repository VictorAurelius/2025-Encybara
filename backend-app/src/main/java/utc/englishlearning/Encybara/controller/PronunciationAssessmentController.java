package utc.englishlearning.Encybara.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utc.englishlearning.Encybara.domain.RestResponse;
import utc.englishlearning.Encybara.exception.PronunciationAssessmentException;
import utc.englishlearning.Encybara.service.PronunciationAssessmentService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pronunciation")
@Slf4j
public class PronunciationAssessmentController {

    private final PronunciationAssessmentService pronunciationAssessmentService;

    public PronunciationAssessmentController(PronunciationAssessmentService pronunciationAssessmentService) {
        this.pronunciationAssessmentService = pronunciationAssessmentService;
    }

    @PostMapping("/assess")
    public ResponseEntity<RestResponse<Map<String, Object>>> assessPronunciation(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "text", required = false) String text) {

        try {
            log.info("Received pronunciation assessment request for file: {}", file.getOriginalFilename());

            // Validate file
            if (file == null || file.isEmpty()) {
                RestResponse<Map<String, Object>> response = new RestResponse<>();
                response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                response.setError("Invalid file");
                response.setMessage("File không được để trống");
                response.setData(null);
                return ResponseEntity.badRequest().body(response);
            }

            // Call pronunciation assessment service
            Map<String, Object> result = pronunciationAssessmentService.assessPronunciation(file, text);

            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setError(null);
            response.setMessage("Pronunciation assessment completed successfully");
            response.setData(result);

            log.info("Pronunciation assessment completed successfully for file: {}", file.getOriginalFilename());
            return ResponseEntity.ok(response);

        } catch (PronunciationAssessmentException e) {
            log.error("Pronunciation assessment service error: {}", e.getMessage());
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(e.getStatusCode());
            response.setError("Pronunciation assessment failed");
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(e.getStatusCode()).body(response);

        } catch (Exception e) {
            log.error("Unexpected error during pronunciation assessment: {}", e.getMessage(), e);
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("Internal server error");
            response.setMessage("Lỗi không mong muốn khi đánh giá phát âm: " + e.getMessage());
            response.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}