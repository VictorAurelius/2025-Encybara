package main.java.utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import utc.englishlearning.Encybara.domain.RestResponse;

@RestController
@RequestMapping("/api/v1/pronunciation")
public class PronunciationAssessmentController {

    private final WebClient webClient;

    @Value("${pronunciation-assessment.service.url}")
    private String serviceUrl;

    public PronunciationAssessmentController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostMapping("/assess")
    public Mono<ResponseEntity<RestResponse>> assessPronunciation(@RequestParam("file") MultipartFile file) {
        return webClient.post()
                .uri(serviceUrl + "/pronunciation/assess")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(file)
                .retrieve()
                .bodyToMono(String.class)
                .map(result -> {
                    RestResponse response = new RestResponse<>();
                    response.setStatusCode(200);
                    response.setError(null);
                    response.setMessage("Pronunciation assessment completed successfully");
                    response.setData(result);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    RestResponse errorResponse = new RestResponse<>();
                    errorResponse.setStatusCode(400);
                    errorResponse.setError("Assessment failed");
                    errorResponse.setMessage("Error assessing pronunciation: " + e.getMessage());
                    errorResponse.setData(null);
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }
}