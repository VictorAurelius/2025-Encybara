package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.perplexity.PerplexityEvaluateRequest;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityEvaluateResponse;
import utc.englishlearning.Encybara.exception.PerplexityException;
import utc.englishlearning.Encybara.service.PerplexityService;

@RestController
@RequestMapping("/api/v1/perplexity")
@RequiredArgsConstructor
public class PerplexityController {

    private final PerplexityService perplexityService;

    @PostMapping("/evaluate")
    public ResponseEntity<RestResponse<PerplexityEvaluateResponse>> evaluateAnswer(
            @RequestBody PerplexityEvaluateRequest request) {

        // Validate request
        if (request.getUserAnswer() == null || request.getQuestion() == null) {
            throw new PerplexityException("User answer and question are required",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Call service and get response
        PerplexityEvaluateResponse evalResponse = perplexityService.evaluateAnswer(
                request.getUserAnswer(),
                request.getQuestion(),
                request.getPrompt());

        // Create and return response
        RestResponse<PerplexityEvaluateResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Answer evaluated successfully");
        response.setData(evalResponse);

        return ResponseEntity.ok(response);
    }
}