package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Value;
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
    private final String apiKey;

    private static class TranslationResponse {
        public static class Data {
            public static class Translation {
                public String translatedText;
            }

            public List<Translation> translations;
        }

        public Data data;
    }

    public GoogleTranslateService(
            WebClient.Builder webClientBuilder,
            @Value("${google.translate.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.baseUrl("https://translation.googleapis.com/language/translate/v2").build();
    }

    public Mono<String> translate(String text, String language) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(Map.of(
                        "q", List.of(text),
                        "target", language))
                .retrieve()
                .bodyToMono(TranslationResponse.class)
                .map(response -> {
                    if (response.data == null || response.data.translations == null
                            || response.data.translations.isEmpty()) {
                        throw new DictionaryException("Translation data not found");
                    }
                    String translatedText = response.data.translations.get(0).translatedText;
                    return translatedText != null ? translatedText : "";
                })
                .onErrorMap(WebClientResponseException.class,
                        ex -> new DictionaryException("Error during translation: " + ex.getMessage()));
    }
}
