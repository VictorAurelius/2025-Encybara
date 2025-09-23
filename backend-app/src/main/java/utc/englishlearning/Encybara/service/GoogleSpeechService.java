package utc.englishlearning.Encybara.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSpeechService {
    private final WebClient webClient;
    private static final String API_KEY = "AIzaSyAWzERmv1eOTgeplOofls-qGgivOj8SEWM";

    public GoogleSpeechService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://speech.googleapis.com/v1").build();
    }
    public Mono<Map<String, Object>> transcribe(MultipartFile file) {
        try {
            byte[] audioBytes = file.getBytes();
            String bas64Audio = Base64.getEncoder().encodeToString(audioBytes);

            return webClient.post()
                    .uri("/speech:recognize?key=" + API_KEY)
                    .bodyValue(Map.of(
                            "config", Map.of(
                                    "encoding", "LINEAR16",
                                    "sampleRateHertz", 16000,
                                    "languageCode", "en-US"
                            ),
                            "audio", Map.of(
                                    "content", bas64Audio
                            )
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        var result = (List<Map<String, Object>>) response.get("results");
                        if (result.isEmpty() || result == null) return Map.of("error", "Không có kết quả");

                        var alternatives= (List<Map<String, Object>>) result.get(0).get("alternatives");
                        if (alternatives.isEmpty() || alternatives == null) return Map.of("error", "Không tìm thấy text");

                        var transcript = (String) alternatives.get(0).get("transcript");
                        var confidence = alternatives.get(0).get("confidence");
                        return Map.of(
                                "transcript", transcript,
                                "confidence", confidence
                        );
                    });
        }catch (Exception e){
            return Mono.error(new RuntimeException("Lỗi xử lí âm thanh"));
        }
    }
}
