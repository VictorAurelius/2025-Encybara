package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import utc.englishlearning.Encybara.domain.Flashcard;
import utc.englishlearning.Encybara.domain.FlashcardGroup;
import utc.englishlearning.Encybara.specification.FlashcardSpecification;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long>, JpaSpecificationExecutor<Flashcard> {

        Page<Flashcard> findAllByOrderByLastReviewedDesc(Pageable pageable);

        Page<Flashcard> findAllByOrderByLastReviewedAsc(Pageable pageable);

        default Page<Flashcard> findAllByFlashcardGroupAndFilters(FlashcardGroup group, String word,
                        Boolean learnedStatus,
                        String vietnameseMeaning, Pageable pageable) {

                Specification<Flashcard> spec = Specification.where(FlashcardSpecification.hasFlashcardGroup(group))
                                .and(FlashcardSpecification.hasWord(word))
                                .and(FlashcardSpecification.hasLearnedStatus(learnedStatus))
                                .and(FlashcardSpecification.hasVietnameseMeaning(vietnameseMeaning));

                return findAll(spec, pageable);
        }
}