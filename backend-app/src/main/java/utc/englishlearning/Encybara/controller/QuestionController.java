package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.question.ReqCreateQuestionDTO;
import utc.englishlearning.Encybara.domain.request.question.ReqUpdateQuestionDTO;
import utc.englishlearning.Encybara.domain.response.question.ResQuestionDTO;
import utc.englishlearning.Encybara.service.QuestionService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping
    public ResponseEntity<RestResponse<ResQuestionDTO>> createQuestion(@RequestBody ReqCreateQuestionDTO questionDTO) {
        ResQuestionDTO createdQuestion = questionService.createQuestion(questionDTO);
        RestResponse<ResQuestionDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Question created successfully");
        response.setData(createdQuestion);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<RestResponse<ResQuestionDTO>> updateQuestion(@RequestBody ReqUpdateQuestionDTO questionDTO) {
        ResQuestionDTO updatedQuestion = questionService.updateQuestion(questionDTO);
        RestResponse<ResQuestionDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Question updated successfully");
        response.setData(updatedQuestion);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteQuestion(@PathVariable("id") Long id) {
        questionService.deleteQuestion(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Question deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResQuestionDTO>> getQuestionById(@PathVariable("id") Long id) {
        ResQuestionDTO question = questionService.getQuestionById(id);
        RestResponse<ResQuestionDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Question retrieved successfully");
        response.setData(question);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RestResponse<Page<ResQuestionDTO>>> getAllQuestions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "quesType", required = false) QuestionTypeEnum quesType,
            @RequestParam(value = "point", required = false) Integer point,
            Pageable pageable) {
        Page<ResQuestionDTO> questions = questionService.getAllQuestions(pageable, keyword, content, quesType, point);
        RestResponse<Page<ResQuestionDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Questions retrieved successfully");
        response.setData(questions);
        return ResponseEntity.ok(response);
    }
}