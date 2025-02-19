package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.Flashcard;
import utc.englishlearning.Encybara.domain.response.flashcard.ResFlashcardDTO;
import utc.englishlearning.Encybara.service.FlashcardService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.request.flashcard.ReqFlashcardDTO;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/flashcards")
public class FlashcardController {

    @Autowired
    private FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<RestResponse<ResFlashcardDTO>> createFlashcard(
            @RequestBody ReqFlashcardDTO reqFlashcardDTO) {
        ResFlashcardDTO flashcard = flashcardService.createFlashcard(
                reqFlashcardDTO.getWord(),
                reqFlashcardDTO.getDefinitionIndices(),
                reqFlashcardDTO.getMeaningIndices(),
                reqFlashcardDTO.getPhoneticIndices(),
                reqFlashcardDTO.getUserId());
        RestResponse<ResFlashcardDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Flashcard created successfully");
        response.setData(flashcard);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{flashcardId}")
    public ResponseEntity<RestResponse<Void>> deleteFlashcard(@PathVariable("flashcardId") Long flashcardId) {
        flashcardService.deleteFlashcard(flashcardId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Flashcard deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{flashcardId}/learned")
    public ResponseEntity<RestResponse<Void>> markFlashcardAsLearned(@PathVariable("flashcardId") Long flashcardId) {
        try {
            flashcardService.markFlashcardAsLearned(flashcardId);
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Flashcard marked as learned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(500);
            response.setMessage("Error marking flashcard as learned: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{flashcardId}/unlearned")
    public ResponseEntity<RestResponse<Void>> markFlashcardAsUnlearned(@PathVariable("flashcardId") Long flashcardId) {
        try {
            flashcardService.markFlashcardAsUnlearned(flashcardId);
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Flashcard marked as unlearned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(500);
            response.setMessage("Error marking flashcard as unlearned: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{flashcardId}")
    public ResponseEntity<RestResponse<ResFlashcardDTO>> getFlashcard(@PathVariable("flashcardId") Long flashcardId) {
        ResFlashcardDTO flashcard = flashcardService.getFlashcard(flashcardId);
        RestResponse<ResFlashcardDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Flashcard retrieved successfully");
        response.setData(flashcard);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sorted/latest")
    public ResponseEntity<RestResponse<Page<ResFlashcardDTO>>> getAllFlashcardsSortedByLatest(Pageable pageable) {
        Page<Flashcard> flashcards = flashcardService.getAllFlashcardsSortedByLatest(pageable);
        RestResponse<Page<ResFlashcardDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Flashcards sorted by last reviewed retrieved successfully");
        response.setData(flashcards.map(flashcard -> {
            ResFlashcardDTO res = new ResFlashcardDTO();
            res.setId(flashcard.getId());
            res.setWord(flashcard.getWord());
            res.setDefinitions(flashcard.getDefinitions());
            res.setExamples(flashcard.getExamples());
            res.setPartOfSpeech(flashcard.getPartOfSpeech());
            res.setPhoneticText(flashcard.getPhoneticText());
            res.setPhoneticAudio(flashcard.getPhoneticAudio());
            res.setVietNameseMeaning(flashcard.getVietNameseMeaning());
            res.setUserId(flashcard.getUser().getId());
            res.setAddedDate(flashcard.getAddedDate());
            res.setLearnedStatus(flashcard.isLearnedStatus());
            res.setLastReviewed(flashcard.getLastReviewed());
            return res;
        }));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sorted/oldest")
    public ResponseEntity<RestResponse<Page<ResFlashcardDTO>>> getAllFlashcardsSortedByOldest(Pageable pageable) {
        Page<Flashcard> flashcards = flashcardService.getAllFlashcardsSortedByOldest(pageable);
        RestResponse<Page<ResFlashcardDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Flashcards sorted by last reviewed retrieved successfully");
        response.setData(flashcards.map(flashcard -> {
            ResFlashcardDTO res = new ResFlashcardDTO();
            res.setId(flashcard.getId());
            res.setWord(flashcard.getWord());
            res.setDefinitions(flashcard.getDefinitions());
            res.setExamples(flashcard.getExamples());
            res.setPartOfSpeech(flashcard.getPartOfSpeech());
            res.setPhoneticText(flashcard.getPhoneticText());
            res.setPhoneticAudio(flashcard.getPhoneticAudio());
            res.setVietNameseMeaning(flashcard.getVietNameseMeaning());
            res.setUserId(flashcard.getUser().getId());
            res.setAddedDate(flashcard.getAddedDate());
            res.setLearnedStatus(flashcard.isLearnedStatus());
            res.setLastReviewed(flashcard.getLastReviewed());
            return res;
        }));
        return ResponseEntity.ok(response);
    }
}