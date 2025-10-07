package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Review;
import utc.englishlearning.Encybara.util.constant.ReviewStatusEnum;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long lessonId);

    Page<Review> findByCourseId(Long lessonId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    Page<Review> findByCourseIdAndNumStarAndStatus(Long lessonId, Integer numStar, ReviewStatusEnum status,
            Pageable pageable);
}