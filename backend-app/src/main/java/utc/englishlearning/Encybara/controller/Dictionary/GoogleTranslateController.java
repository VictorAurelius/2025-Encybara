package utc.englishlearning.Encybara.controller.Dictionary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import utc.englishlearning.Encybara.service.GoogleTranslateService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

@RestController
@RequestMapping("/api/v1/dictionary")
public class GoogleTranslateController {
    private final GoogleTranslateService googleTranslateService;

    public GoogleTranslateController(GoogleTranslateService googleTranslateService) {
        this.googleTranslateService = googleTranslateService;
    }

    @GetMapping("/translate")
    public ResponseEntity<RestResponse<String>> translateText(
            @RequestParam String text,
            @RequestParam(defaultValue = "vi") String language) {
        Mono<String> translatedTextMono = googleTranslateService.translate(text, language);

        // Chuyển đổi Mono<String> thành String
        String translatedText = translatedTextMono.block(); // Chặn để lấy giá trị

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Translation successful");
        response.setData(translatedText);
        return ResponseEntity.ok(response);
    }
}
