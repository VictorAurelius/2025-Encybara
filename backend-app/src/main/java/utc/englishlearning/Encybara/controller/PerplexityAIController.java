package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.perplexity.PerplexityRequest;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.perplexity.PerplexityResponse;
import utc.englishlearning.Encybara.exception.PerplexityException;
import utc.englishlearning.Encybara.service.PerplexityAIService;

@RestController
@RequestMapping("/api/v1/perplexity") // Using chatgpt endpoint for compatibility
@RequiredArgsConstructor
public class PerplexityAIController {

    private final PerplexityAIService perplexityAIService;

    @PostMapping("/evaluate")
    public ResponseEntity<RestResponse<PerplexityResponse>> evaluateAnswer(
            @RequestBody PerplexityRequest request) {

        // Validate request
        if (request.getUserAnswer() == null || request.getQuestion() == null) {
            throw new PerplexityException("User answer and question are required",
                    HttpStatus.BAD_REQUEST.value());
        }

        // Call service and get response
        PerplexityResponse evalResponse = perplexityAIService.evaluateAnswer(
                request.getQuestion(),
                request.getUserAnswer(),
                request.getPrompt());

        // Create and return response
        RestResponse<PerplexityResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Answer evaluated successfully");
        response.setData(evalResponse);

        return ResponseEntity.ok(response);
    }
}