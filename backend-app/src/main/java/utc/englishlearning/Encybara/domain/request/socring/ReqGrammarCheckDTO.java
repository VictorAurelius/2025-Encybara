package utc.englishlearning.Encybara.domain.request.socring;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqGrammarCheckDTO {
    private String text;
    private String language= "en-US";
}
