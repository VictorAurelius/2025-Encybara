package utc.englishlearning.Encybara.specification;

import org.springframework.data.jpa.domain.Specification;
import utc.englishlearning.Encybara.domain.Flashcard;
import utc.englishlearning.Encybara.domain.FlashcardGroup;

public class FlashcardSpecification {

    public static Specification<Flashcard> hasFlashcardGroup(FlashcardGroup group) {
        return (root, query, criteriaBuilder) -> group == null ? null
                : criteriaBuilder.equal(root.get("flashcardGroup"), group);
    }

    public static Specification<Flashcard> hasWord(String word) {
        return (root, query, criteriaBuilder) -> word == null || word.isEmpty() ? null
                : criteriaBuilder.like(root.get("word"), "%" + word + "%");
    }

    public static Specification<Flashcard> hasLearnedStatus(Boolean learnedStatus) {
        return (root, query, criteriaBuilder) -> learnedStatus == null ? null
                : criteriaBuilder.equal(root.get("learnedStatus"), learnedStatus);
    }

    public static Specification<Flashcard> hasVietnameseMeaning(String vietnameseMeaning) {
        return (root, query, criteriaBuilder) -> vietnameseMeaning == null || vietnameseMeaning.isEmpty() ? null
                : criteriaBuilder.like(root.get("vietNameseMeaning"), "%" + vietnameseMeaning + "%");
    }
}