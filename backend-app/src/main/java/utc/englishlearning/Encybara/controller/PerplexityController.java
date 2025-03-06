package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.perplexity.PerplexityAuthCodeRequest;
import utc.englishlearning.Encybara.domain.request.perplexity.PerplexityEvaluateRequest;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityEvaluateResponse;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.exception.PerplexityException;
import utc.englishlearning.Encybara.service.PerplexityService;

import jakarta.annotation.PreDestroy;

@RestController
@RequestMapping("/api/perplexity")
@RequiredArgsConstructor
public class PerplexityController {

    private final PerplexityService perplexityService;

    @PostMapping("/init")
    public ResponseEntity<RestResponse<String>> initializeSession() {
        boolean success = perplexityService.initiateLogin();
        if (!success) {
            throw new PerplexityException("Failed to initialize Perplexity session",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Please check your email for the authentication code");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<RestResponse<String>> verifyAuthCode(@RequestBody PerplexityAuthCodeRequest request) {
        boolean success = perplexityService.submitAuthCode(request.getAuthCode());
        if (!success) {
            throw new PerplexityException("Failed to verify authentication code",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Authentication successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<RestResponse<PerplexityEvaluateResponse>> evaluateAnswer(
            @RequestBody PerplexityEvaluateRequest request) {
        if (request.getUserAnswer() == null || request.getQuestion() == null) {
            throw new PerplexityException("User answer and question are required", HttpStatus.BAD_REQUEST.value());
        }

        PerplexityEvaluateResponse evalResponse = perplexityService.evaluateAnswer(
                request.getUserAnswer(),
                request.getQuestion(),
                request.getPrompt());

        RestResponse<PerplexityEvaluateResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(evalResponse);
        response.setMessage("Answer evaluated successfully");
        return ResponseEntity.ok(response);
    }

    @PreDestroy
    public void cleanup() {
        perplexityService.cleanup();
    }
}