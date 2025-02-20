package utc.englishlearning.Encybara.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import utc.englishlearning.Encybara.domain.request.socring.ReqGrammarCheckDTO;
import utc.englishlearning.Encybara.domain.response.scoring.ResGrammarCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GrammarCheckService {
    private final String API_URL = "https://api.languagetool.org/v2/check";
    private final RestTemplate restTemplate = new RestTemplate();

    public ResGrammarCheck checkGrammar(ReqGrammarCheckDTO requestDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("text", requestDTO.getText());
        params.add("language", requestDTO.getLanguage());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);

        List<String> errors = new ArrayList<>();
        int errorCount = 0;

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> matches = (List<Map<String, Object>>) response.getBody().get("matches");
            errorCount = matches.size();

            for (Map<String, Object> match : matches) {
                errors.add((String) match.get("message"));
            }
        }

        int score = Math.max(0, 100 - errorCount * 5);
        return new ResGrammarCheck(errors, errorCount, score);
    }
}
