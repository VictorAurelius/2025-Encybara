package utc.englishlearning.Encybara.domain.response.dictionary;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResWord {
    private String word;
    private List<Phonetic> phonetics;
    private List<Meaning> meanings;
}


