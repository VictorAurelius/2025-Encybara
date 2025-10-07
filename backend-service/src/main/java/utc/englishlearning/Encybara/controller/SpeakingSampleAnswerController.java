package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.speaking.ReqCreateSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.domain.request.speaking.ReqUpdateSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.domain.response.speaking.ResSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.service.SpeakingSampleAnswerService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/speaking-sample-answers")
public class SpeakingSampleAnswerController {

    @Autowired
    private SpeakingSampleAnswerService speakingSampleAnswerService;

    /**
     * Create a new speaking sample answer
     */
    @PostMapping
    @ApiMessage("Create speaking sample answer")
    public ResponseEntity<RestResponse<ResSpeakingSampleAnswerDTO>> createSpeakingSampleAnswer(
            @Valid @RequestBody ReqCreateSpeakingSampleAnswerDTO requestDTO) {

        ResSpeakingSampleAnswerDTO createdAnswer = speakingSampleAnswerService.createSpeakingSampleAnswer(requestDTO);
        RestResponse<ResSpeakingSampleAnswerDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Speaking sample answer created successfully");
        response.setData(createdAnswer);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing speaking sample answer
     */
    @PutMapping
    @ApiMessage("Update speaking sample answer")
    public ResponseEntity<RestResponse<ResSpeakingSampleAnswerDTO>> updateSpeakingSampleAnswer(
            @Valid @RequestBody ReqUpdateSpeakingSampleAnswerDTO requestDTO) {

        ResSpeakingSampleAnswerDTO updatedAnswer = speakingSampleAnswerService.updateSpeakingSampleAnswer(requestDTO);
        RestResponse<ResSpeakingSampleAnswerDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Speaking sample answer updated successfully");
        response.setData(updatedAnswer);
        return ResponseEntity.ok(response);
    }

    /**
     * Get speaking sample answer by ID
     */
    @GetMapping("/{id}")
    @ApiMessage("Get speaking sample answer by ID")
    public ResponseEntity<RestResponse<ResSpeakingSampleAnswerDTO>> getSpeakingSampleAnswerById(
            @PathVariable Long id) {

        ResSpeakingSampleAnswerDTO sampleAnswer = speakingSampleAnswerService.getSpeakingSampleAnswerById(id);
        RestResponse<ResSpeakingSampleAnswerDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Speaking sample answer retrieved successfully");
        response.setData(sampleAnswer);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all sample answers for a specific question
     */
    @GetMapping("/question/{questionId}")
    @ApiMessage("Get sample answers by question ID")
    public ResponseEntity<RestResponse<List<ResSpeakingSampleAnswerDTO>>> getSampleAnswersByQuestionId(
            @PathVariable Long questionId) {

        List<ResSpeakingSampleAnswerDTO> sampleAnswers = speakingSampleAnswerService
                .getSampleAnswersByQuestionId(questionId);
        RestResponse<List<ResSpeakingSampleAnswerDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Sample answers retrieved successfully");
        response.setData(sampleAnswers);
        return ResponseEntity.ok(response);
    }

    /**
     * Get sample answers by question ID and difficulty level
     */
    @GetMapping("/question/{questionId}/difficulty/{difficultyLevel}")
    @ApiMessage("Get sample answers by question ID and difficulty")
    public ResponseEntity<RestResponse<List<ResSpeakingSampleAnswerDTO>>> getSampleAnswersByQuestionIdAndDifficulty(
            @PathVariable Long questionId,
            @PathVariable Integer difficultyLevel) {

        List<ResSpeakingSampleAnswerDTO> sampleAnswers = speakingSampleAnswerService
                .getSampleAnswersByQuestionIdAndDifficulty(questionId, difficultyLevel);
        RestResponse<List<ResSpeakingSampleAnswerDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Sample answers retrieved successfully");
        response.setData(sampleAnswers);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a speaking sample answer
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Delete speaking sample answer")
    public ResponseEntity<RestResponse<Void>> deleteSpeakingSampleAnswer(@PathVariable Long id) {

        speakingSampleAnswerService.deleteSpeakingSampleAnswer(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Speaking sample answer deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Check if a question has sample answers
     */
    @GetMapping("/question/{questionId}/exists")
    @ApiMessage("Check if question has sample answers")
    public ResponseEntity<RestResponse<Boolean>> checkQuestionHasSampleAnswers(@PathVariable Long questionId) {

        boolean hasSampleAnswers = speakingSampleAnswerService.questionHasSampleAnswers(questionId);
        RestResponse<Boolean> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Check completed successfully");
        response.setData(hasSampleAnswers);
        return ResponseEntity.ok(response);
    }

    /**
     * Get count of sample answers for a question
     */
    @GetMapping("/question/{questionId}/count")
    @ApiMessage("Get count of sample answers for question")
    public ResponseEntity<RestResponse<Long>> countSampleAnswersByQuestionId(@PathVariable Long questionId) {

        Long count = speakingSampleAnswerService.countSampleAnswersByQuestionId(questionId);
        RestResponse<Long> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Count retrieved successfully");
        response.setData(count);
        return ResponseEntity.ok(response);
    }
}