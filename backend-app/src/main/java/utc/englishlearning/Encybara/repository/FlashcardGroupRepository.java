package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import utc.englishlearning.Encybara.domain.FlashcardGroup;

public interface FlashcardGroupRepository extends JpaRepository<FlashcardGroup, Long> {
    FlashcardGroup findByName(String name);

    boolean existsByName(String name);

    Page<FlashcardGroup> findAllByUserId(Long userId, Pageable pageable);
}