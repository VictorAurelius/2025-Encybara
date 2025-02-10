package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.Like;
import utc.englishlearning.Encybara.domain.Review;
import utc.englishlearning.Encybara.domain.Discussion;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.LikeRepository;
import utc.englishlearning.Encybara.repository.ReviewRepository;
import utc.englishlearning.Encybara.repository.DiscussionRepository;
import utc.englishlearning.Encybara.repository.UserRepository;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Transactional
    public void likeReview(Long userId, Long reviewId) {
        if (likeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            throw new RuntimeException("User has already liked this review.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        Like like = new Like();
        like.setUser(user);
        like.setReview(review);
        likeRepository.save(like);

        // Tăng số lượng like cho review
        review.setNumLike(review.getNumLike() + 1);
        reviewRepository.save(review);
    }

    @Transactional
    public void unlikeReview(Long userId, Long reviewId) {
        if (!likeRepository.existsByUserIdAndReviewId(userId, reviewId)) {
            throw new RuntimeException("User has not liked this review.");
        }

        likeRepository.deleteByUserIdAndReviewId(userId, reviewId);

        // Giảm số lượng like cho review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setNumLike(review.getNumLike() - 1);
        reviewRepository.save(review);
    }

    @Transactional
    public void likeDiscussion(Long userId, Long discussionId) {
        if (likeRepository.existsByUserIdAndDiscussionId(userId, discussionId)) {
            throw new RuntimeException("User has already liked this discussion.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));

        Like like = new Like();
        like.setUser(user);
        like.setDiscussion(discussion);
        likeRepository.save(like);

        // Tăng số lượng like cho discussion
        discussion.setNumLike(discussion.getNumLike() + 1);
        discussionRepository.save(discussion);
    }

    @Transactional
    public void unlikeDiscussion(Long userId, Long discussionId) {
        if (!likeRepository.existsByUserIdAndDiscussionId(userId, discussionId)) {
            throw new RuntimeException("User has not liked this discussion.");
        }

        likeRepository.deleteByUserIdAndDiscussionId(userId, discussionId);

        // Giảm số lượng like cho discussion
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("Discussion not found"));
        discussion.setNumLike(discussion.getNumLike() - 1);
        discussionRepository.save(discussion);
    }
}