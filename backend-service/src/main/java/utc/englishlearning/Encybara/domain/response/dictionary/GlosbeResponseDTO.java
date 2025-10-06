package utc.englishlearning.Encybara.domain.response.dictionary;

import java.util.List;

public class GlosbeResponseDTO {
    private String word;
    private List<Meaning> meanings;

    public GlosbeResponseDTO(String word, List<Meaning> meanings) {
        this.word = word;
        this.meanings = meanings;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<Meaning> meanings) {
        this.meanings = meanings;
    }

    public static class Meaning {
        private String translateMeaning;
        private String partOfSpeech;
        private String example;
        private String exampleMeaning;

        public Meaning(String translateMeaning, String partOfSpeech, String example, String exampleMeaning) {
            this.translateMeaning = translateMeaning;
            this.partOfSpeech = partOfSpeech;
            this.example = example;
            this.exampleMeaning = exampleMeaning;
        }

        public String getTranslateMeaning() {
            return translateMeaning;
        }

        public void setTranslateMeaning(String translateMeaning) {
            this.translateMeaning = translateMeaning;
        }

        public String getPartOfSpeech() {
            return partOfSpeech;
        }

        public void setPartOfSpeech(String partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        public String getExample() {
            return example;
        }

        public void setExample(String example) {
            this.example = example;
        }

        public String getExampleMeaning() {
            return exampleMeaning;
        }

        public void setExampleMeaning(String exampleMeaning) {
            this.exampleMeaning = exampleMeaning;
        }
    }
}