package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import utc.englishlearning.Encybara.domain.request.speaking.ReqCreateSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.domain.request.speaking.ReqUpdateSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.domain.response.speaking.ResSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.service.SpeakingSampleAnswerService;
import utc.englishlearning.Encybara.service.LearningMaterialService;
import utc.englishlearning.Encybara.service.FileStorageService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;
import utc.englishlearning.Encybara.domain.SpeakingSampleAnswer;
import utc.englishlearning.Encybara.repository.SpeakingSampleAnswerRepository;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/speaking-sample-answers")
public class SpeakingSampleAnswerController {

    @Autowired
    private SpeakingSampleAnswerService speakingSampleAnswerService;

    @Autowired
    private LearningMaterialService learningMaterialService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private SpeakingSampleAnswerRepository speakingSampleAnswerRepository;

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

    /**
     * Upload audio file for a speaking sample answer
     */
    @PostMapping("/{id}/upload-audio")
    @ApiMessage("Upload audio file for speaking sample answer")
    public ResponseEntity<RestResponse<String>> uploadAudioForSampleAnswer(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        // Validate speaking sample answer exists
        SpeakingSampleAnswer sampleAnswer = speakingSampleAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaking sample answer not found with id: " + id));

        // Validate file
        if (file == null || file.isEmpty()) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(400);
            response.setMessage("Audio file is empty. Please upload a file.");
            return ResponseEntity.badRequest().body(response);
        }

        // Validate audio file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(400);
            response.setMessage("File must be an audio format (mp3, wav, etc.)");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Store audio file in speaking-sample-answers directory
            String audioLink = learningMaterialService.store(file, "speaking-sample-answers");

            // Update sample answer with audio link
            sampleAnswer.setAudioLink(audioLink);
            speakingSampleAnswerRepository.save(sampleAnswer);

            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Audio file uploaded successfully");
            response.setData(audioLink);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(500);
            response.setMessage("Failed to upload audio file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(500);
            response.setMessage("Failed to upload audio file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get audio link for a speaking sample answer
     */
    @GetMapping("/{id}/audio-link")
    @ApiMessage("Get audio link for speaking sample answer")
    public ResponseEntity<RestResponse<String>> getAudioLinkBySampleAnswerId(@PathVariable Long id) {
        SpeakingSampleAnswer sampleAnswer = speakingSampleAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaking sample answer not found with id: " + id));

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Audio link retrieved successfully");
        response.setData(sampleAnswer.getAudioLink());
        return ResponseEntity.ok(response);
    }

    /**
     * Update audio link for a speaking sample answer
     */
    @PutMapping("/{id}/audio-link")
    @ApiMessage("Update audio link for speaking sample answer")
    public ResponseEntity<RestResponse<String>> updateAudioLink(
            @PathVariable Long id,
            @RequestParam("audioLink") String audioLink) {

        SpeakingSampleAnswer sampleAnswer = speakingSampleAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaking sample answer not found with id: " + id));

        sampleAnswer.setAudioLink(audioLink);
        speakingSampleAnswerRepository.save(sampleAnswer);

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Audio link updated successfully");
        response.setData(audioLink);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete audio for a speaking sample answer
     */
    @DeleteMapping("/{id}/audio")
    @ApiMessage("Delete audio for speaking sample answer")
    public ResponseEntity<RestResponse<Void>> deleteAudioForSampleAnswer(@PathVariable Long id) {
        SpeakingSampleAnswer sampleAnswer = speakingSampleAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaking sample answer not found with id: " + id));

        String audioLink = sampleAnswer.getAudioLink();
        if (audioLink != null && !audioLink.isEmpty()) {
            try {
                // Delete physical file
                fileStorageService.deleteFile(audioLink);
            } catch (Exception e) {
                // Log error but continue with database update
                System.err.println("Error deleting audio file: " + e.getMessage());
            }

            // Remove audio link from database
            sampleAnswer.setAudioLink(null);
            speakingSampleAnswerRepository.save(sampleAnswer);
        }

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Audio file deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all sample answers with audio links for a question
     */
    @GetMapping("/question/{questionId}/with-audio")
    @ApiMessage("Get sample answers with audio links by question ID")
    public ResponseEntity<RestResponse<List<ResSpeakingSampleAnswerDTO>>> getSampleAnswersWithAudioByQuestionId(
            @PathVariable Long questionId) {

        List<ResSpeakingSampleAnswerDTO> sampleAnswers = speakingSampleAnswerService
                .getSampleAnswersByQuestionId(questionId);

        // Filter only answers that have audio links
        List<ResSpeakingSampleAnswerDTO> answersWithAudio = sampleAnswers.stream()
                .filter(answer -> answer.getAudioLink() != null && !answer.getAudioLink().isEmpty())
                .toList();

        RestResponse<List<ResSpeakingSampleAnswerDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Sample answers with audio retrieved successfully");
        response.setData(answersWithAudio);
        return ResponseEntity.ok(response);
    }
}