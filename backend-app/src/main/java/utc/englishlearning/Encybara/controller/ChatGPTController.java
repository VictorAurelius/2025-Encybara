package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.chatgpt.ChatGPTEvaluateRequest;
import utc.englishlearning.Encybara.domain.response.chatgpt.ChatGPTEvaluateResponse;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.exception.ChatGPTException;
import utc.englishlearning.Encybara.service.ChatGPTService;

import jakarta.annotation.PreDestroy;

@RestController
@RequestMapping("/api/chatgpt")
@RequiredArgsConstructor
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    @PostMapping("/evaluate")
    public ResponseEntity<RestResponse<ChatGPTEvaluateResponse>> evaluateAnswer(
            @RequestBody ChatGPTEvaluateRequest request) {
        if (request.getUserAnswer() == null || request.getQuestion() == null) {
            throw new ChatGPTException("User answer and question are required", HttpStatus.BAD_REQUEST.value());
        }

        ChatGPTEvaluateResponse evalResponse = chatGPTService.evaluateAnswer(
                request.getUserAnswer(),
                request.getQuestion(),
                request.getPrompt());

        RestResponse<ChatGPTEvaluateResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setData(evalResponse);
        response.setMessage("Answer evaluated successfully");
        return ResponseEntity.ok(response);
    }

    @PreDestroy
    public void cleanup() {
        chatGPTService.cleanup();
    }
}