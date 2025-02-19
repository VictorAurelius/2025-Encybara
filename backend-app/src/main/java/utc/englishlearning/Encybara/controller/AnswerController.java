package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.Answer;
import utc.englishlearning.Encybara.domain.response.answer.ResAnswerDTO;
import utc.englishlearning.Encybara.domain.request.answer.ReqCreateAnswerDTO;
import utc.englishlearning.Encybara.service.AnswerService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

@RestController
@RequestMapping("/api/v1/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    // @PostMapping
    // public ResponseEntity<RestResponse<ResAnswerDTO>> createAnswer(@RequestBody
    // ReqCreateAnswerDTO reqCreateAnswerDTO) {
    // ResAnswerDTO createdAnswer = answerService.createAnswer(reqCreateAnswerDTO);
    // RestResponse<ResAnswerDTO> response = new RestResponse<>();
    // response.setStatusCode(200);
    // response.setMessage("Answer created successfully");
    // response.setData(createdAnswer);
    // return ResponseEntity.ok(response);
    // }

    @PostMapping("/user/{userId}")
    public ResponseEntity<RestResponse<ResAnswerDTO>> createAnswerWithUserId(
            @RequestBody ReqCreateAnswerDTO reqCreateAnswerDTO, @PathVariable("userId") Long userId) {
        ResAnswerDTO createdAnswer = answerService.createAnswerWithUserId(reqCreateAnswerDTO, userId);
        RestResponse<ResAnswerDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Answer created successfully with user ID");
        response.setData(createdAnswer);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResAnswerDTO>> getAnswerById(@PathVariable("id") Long id) {
        ResAnswerDTO answer = answerService.getAnswerById(id);
        RestResponse<ResAnswerDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Answer retrieved successfully");
        response.setData(answer);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<RestResponse<Page<Answer>>> getAnswersByQuestionId(
            @PathVariable("questionId") Long questionId, Pageable pageable) {
        Page<Answer> answers = answerService.getAnswersByQuestionId(questionId, pageable);
        RestResponse<Page<Answer>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Answers retrieved successfully");
        response.setData(answers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/question/{questionId}/user/{userId}")
    public ResponseEntity<RestResponse<Page<Answer>>> getAllAnswersByQuestionIdAndUserId(
            @PathVariable("questionId") Long questionId, @PathVariable("userId") Long userId, Pageable pageable) {
        Page<Answer> answers = answerService.getAllAnswersByQuestionIdAndUserId(questionId, userId, pageable);
        RestResponse<Page<Answer>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Answers retrieved successfully");
        response.setData(answers);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/grade/{answerId}")
    public ResponseEntity<RestResponse<Void>> gradeAnswer(@PathVariable("answerId") Long answerId) {
        answerService.gradeAnswer(answerId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Answer graded successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<ResAnswerDTO> getLatestAnswer(@RequestParam Long questionId, @RequestParam Long userId) {
        ResAnswerDTO latestAnswer = answerService.getLatestAnswerByUserAndQuestion(questionId, userId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Lastest answer request successfully");
        return ResponseEntity.ok(latestAnswer);
    }
}