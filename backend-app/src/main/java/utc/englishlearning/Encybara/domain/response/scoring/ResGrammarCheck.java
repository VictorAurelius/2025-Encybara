package utc.englishlearning.Encybara.domain.response.scoring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ResGrammarCheck {
    private List<String> errors;
    private int errorCount;
    private int score;
}


