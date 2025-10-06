package utc.englishlearning.Encybara.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.response.dictionary.GlosbeResponseDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GlosbeDictionaryService {

    public GlosbeResponseDTO defineWord(String word) throws IOException {
        return fetchTranslations("https://vi.glosbe.com/en/vi/" + word);
    }

    public GlosbeResponseDTO translateVietnameseToEnglish(String text) throws IOException {
        return fetchTranslations("https://vi.glosbe.com/vi/en/" + text);
    }

    public Mono<GlosbeResponseDTO> getWordDefinition(String word) throws IOException {
        return Mono.fromCallable(() -> defineWord(word));
    }

    private GlosbeResponseDTO fetchTranslations(String url) throws IOException {
        List<GlosbeResponseDTO.Meaning> meanings = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        // Cào các thẻ li có data-element="translation"
        Elements translationElements = doc.select("li[data-element='translation']");

        for (Element translation : translationElements) {
            String translateMeaning = translation.selectFirst("h3") != null
                    ? translation.selectFirst("h3").text().trim()
                    : null;
            String partOfSpeech = translation.selectFirst("span.inline-block.dir-aware-pr-1") != null
                    ? translation.selectFirst("span.inline-block.dir-aware-pr-1").text().trim()
                    : null;

            // Cào example và example_meaning
            Elements exampleElements = translation.select("div.translation__example");
            String example = exampleElements.select("p[lang='en']").text().trim();
            String exampleMeaning = exampleElements.select("p:not([lang='en'])").text().trim();

            // Kiểm tra xem có đủ thông tin không
            if (translateMeaning != null && partOfSpeech != null && !example.isEmpty() && !exampleMeaning.isEmpty()) {
                meanings.add(new GlosbeResponseDTO.Meaning(translateMeaning, partOfSpeech, example, exampleMeaning));
            }
        }

        // Nếu không tìm thấy bản dịch nào
        if (meanings.isEmpty()) {
            throw new ResourceNotFoundException("No valid translations found for the word: " + url);
        }

        return new GlosbeResponseDTO(url, meanings);
    }
}