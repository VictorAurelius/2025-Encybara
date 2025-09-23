package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.service.LikeService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/review")
    public ResponseEntity<RestResponse<Void>> likeReview(@RequestParam("userId") Long userId,
            @RequestParam("reviewId") Long reviewId) {
        likeService.likeReview(userId, reviewId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review liked successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/review")
    public ResponseEntity<RestResponse<Void>> unlikeReview(@RequestParam("userId") Long userId,
            @RequestParam("reviewId") Long reviewId) {
        likeService.unlikeReview(userId, reviewId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review unliked successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/discussion")
    public ResponseEntity<RestResponse<Void>> likeDiscussion(@RequestParam("userId") Long userId,
            @RequestParam("discussionId") Long discussionId) {
        likeService.likeDiscussion(userId, discussionId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion liked successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/discussion")
    public ResponseEntity<RestResponse<Void>> unlikeDiscussion(@RequestParam("userId") Long userId,
            @RequestParam("discussionId") Long discussionId) {
        likeService.unlikeDiscussion(userId, discussionId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion unliked successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/review-is-liked")
    public ResponseEntity<Boolean> isReviewLiked(@RequestParam("userId") Long userId,
            @RequestParam("reviewId") Long reviewId) {
        boolean isLiked = likeService.isReviewLiked(userId, reviewId);
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping("/discussion-is-liked")
    public ResponseEntity<Boolean> isDiscussionLiked(@RequestParam("userId") Long userId,
            @RequestParam("discussionId") Long discussionId) {
        boolean isLiked = likeService.isDiscussionLiked(userId, discussionId);
        return ResponseEntity.ok(isLiked);
    }
}