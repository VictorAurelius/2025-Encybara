package utc.englishlearning.Encybara.controller;

import io.micrometer.core.ipc.http.HttpSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import utc.englishlearning.Encybara.service.GoogleSpeechService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/speech")
public class GoogleSpeechController {
    private final GoogleSpeechService googleSpeechService;

    public GoogleSpeechController(GoogleSpeechService googleSpeechService) {
        this.googleSpeechService = googleSpeechService;
    }

    @PostMapping("/convert")
    public Mono<ResponseEntity<Map<String, Object>>> convertSpeechToText(@RequestParam("file") MultipartFile file) {
        return  googleSpeechService.transcribe(file)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Invalid file"))));
    }
}
