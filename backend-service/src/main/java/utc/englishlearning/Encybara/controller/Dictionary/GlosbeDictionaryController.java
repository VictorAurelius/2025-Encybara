package utc.englishlearning.Encybara.controller.Dictionary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utc.englishlearning.Encybara.domain.response.dictionary.GlosbeResponseDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.service.GlosbeDictionaryService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/glosbe")
public class GlosbeDictionaryController {

    @Autowired
    private GlosbeDictionaryService glosbeDictionaryService;

    @GetMapping("/define/{word}")
    public ResponseEntity<RestResponse<GlosbeResponseDTO>> defineWord(@PathVariable("word") String word) {
        RestResponse<GlosbeResponseDTO> response = new RestResponse<>();
        try {
            GlosbeResponseDTO responseDTO = glosbeDictionaryService.defineWord(word);

            // Kiểm tra nếu không có nghĩa nào
            if (responseDTO.getMeanings().isEmpty()) {
                throw new ResourceNotFoundException("No valid translations found for the word: " + word);
            }

            response.setStatusCode(200);
            response.setMessage("Translations retrieved successfully");
            response.setData(responseDTO);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            response.setData(null); // Không có dữ liệu
            return ResponseEntity.status(404).body(response);
        } catch (IOException e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching data: " + e.getMessage());
            response.setData(null); // Không có dữ liệu
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/translate/{text}")
    public ResponseEntity<RestResponse<GlosbeResponseDTO>> translate(@PathVariable("text") String text) {
        RestResponse<GlosbeResponseDTO> response = new RestResponse<>();
        try {
            GlosbeResponseDTO responseDTO = glosbeDictionaryService.translateVietnameseToEnglish(text);
            response.setStatusCode(200);
            response.setMessage("Translation retrieved successfully");
            response.setData(responseDTO);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            response.setData(null); // Không có dữ liệu
            return ResponseEntity.status(404).body(response);
        } catch (IOException e) {
            response.setStatusCode(500);
            response.setMessage("Error fetching data: " + e.getMessage());
            response.setData(null); // Không có dữ liệu
            return ResponseEntity.status(500).body(response);
        }
    }
}