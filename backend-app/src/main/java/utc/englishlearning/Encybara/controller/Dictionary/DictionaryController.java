package utc.englishlearning.Encybara.controller.Dictionary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.response.dictionary.ResWord;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.service.DictionaryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dictionary")
public class DictionaryController {
    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/{word}")
    public ResponseEntity<RestResponse<List<ResWord>>> getWordDefinitions(@PathVariable String word) {
        List<ResWord> definitions = dictionaryService.getWordDefinition(word).block();
        RestResponse<List<ResWord>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Definitions retrieved successfully");
        response.setData(definitions);
        return ResponseEntity.ok(response);
    }
}
