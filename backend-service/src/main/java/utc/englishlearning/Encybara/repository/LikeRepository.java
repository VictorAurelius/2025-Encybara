package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Like;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    boolean existsByUserIdAndDiscussionId(Long userId, Long discussionId);

    void deleteByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByUserIdAndDiscussionId(Long userId, Long discussionId);
}