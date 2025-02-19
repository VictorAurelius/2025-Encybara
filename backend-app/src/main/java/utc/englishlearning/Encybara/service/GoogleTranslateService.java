package utc.englishlearning.Encybara.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

import utc.englishlearning.Encybara.exception.DictionaryException;

@Service
public class GoogleTranslateService {
    private final WebClient webClient;
    private static final String API_KEY = "AIzaSyAWzERmv1eOTgeplOofls-qGgivOj8SEWM";

    public GoogleTranslateService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://translation.googleapis.com/language/translate/v2").build();
    }

    public Mono<String> translate(String text, String language) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", API_KEY)
                        .build())
                .bodyValue(Map.of(
                        "q", List.of(text),
                        "target", language))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object data = response.get("data");
                    if (data instanceof Map) {
                        List<Map<String, Object>> translations = (List<Map<String, Object>>) ((Map<String, Object>) data)
                                .get("translations");
                        return translations.isEmpty() ? null : (String) translations.get(0).get("translatedText");
                    }
                    throw new DictionaryException("Translation data not found");
                })
                .onErrorMap(WebClientResponseException.class,
                        ex -> new DictionaryException("Error during translation: " + ex.getMessage()));
    }
}
