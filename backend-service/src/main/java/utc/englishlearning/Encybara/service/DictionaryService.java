package utc.englishlearning.Encybara.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import utc.englishlearning.Encybara.domain.response.dictionary.Phonetic;
import utc.englishlearning.Encybara.domain.response.dictionary.ResWord;
import utc.englishlearning.Encybara.exception.DictionaryException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DictionaryService {
    private final WebClient webClient;

    public DictionaryService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.dictionaryapi.dev/api/v2/entries/en").build();
    }

    public Mono<List<ResWord>> getWordDefinition(String word) {
        return webClient.get()
                .uri("/{word}", word)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ResWord>>() {
                })
                .doOnSuccess(res -> {
                    res.forEach(r -> {
                        // Lọc và chỉ lấy phonetic có audio không rỗng
                        List<Phonetic> validPhonetics = r.getPhonetics().stream()
                                .filter(p -> p.getAudio() != null && !p.getAudio().isEmpty() && p.getText() != null
                                        && !p.getText().isEmpty())
                                .collect(Collectors.toList());

                        // Nếu có phonetic hợp lệ, gán lại phonetics
                        if (!validPhonetics.isEmpty()) {
                            // Bạn có thể chọn phonetic đầu tiên hoặc theo tiêu chí khác
                            r.setPhonetics(validPhonetics.subList(0, 1)); // Chỉ lấy phonetic đầu tiên có audio
                        } else {
                            r.setPhonetics(Collections.emptyList()); // Không có phonetic hợp lệ
                        }
                    });
                })
                .onErrorMap(WebClientResponseException.class,
                        ex -> new DictionaryException("Error retrieving word definition: " + ex.getMessage()));
    }

}
