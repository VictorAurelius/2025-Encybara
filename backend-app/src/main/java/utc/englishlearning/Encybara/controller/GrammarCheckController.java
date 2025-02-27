package utc.englishlearning.Encybara.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utc.englishlearning.Encybara.domain.RestResponse;
import utc.englishlearning.Encybara.domain.request.socring.ReqGrammarCheckDTO;
import utc.englishlearning.Encybara.domain.response.scoring.ResGrammarCheck;
import utc.englishlearning.Encybara.service.GrammarCheckService;

@RestController
@RequestMapping("/api/v1/grammar")
public class GrammarCheckController {
    private final GrammarCheckService grammarCheckService;

    public GrammarCheckController(GrammarCheckService grammarCheckService) {
        this.grammarCheckService = grammarCheckService;
    }

    @PostMapping("/check")
    public ResponseEntity<RestResponse<ResGrammarCheck>> checkGrammar(@RequestBody ReqGrammarCheckDTO requestDTO) {
        ResGrammarCheck result = grammarCheckService.checkGrammar(requestDTO);
        RestResponse restResponse = new RestResponse();
        restResponse.setData(result);
        restResponse.setMessage("Grammar Check Success");
        restResponse.setStatusCode(200);
        return ResponseEntity.ok(restResponse);
    }
}
