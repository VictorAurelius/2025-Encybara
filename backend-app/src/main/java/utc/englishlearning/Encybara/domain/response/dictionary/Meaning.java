package utc.englishlearning.Encybara.domain.response.dictionary;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Meaning {
    private String partOfSpeech;
    private List<Definition> definitions;

}
