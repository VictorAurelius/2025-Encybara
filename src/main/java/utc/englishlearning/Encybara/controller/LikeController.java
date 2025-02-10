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
    public ResponseEntity<RestResponse<Void>> likeReview(@RequestParam Long userId,
            @RequestParam Long reviewId) {
        likeService.likeReview(userId, reviewId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review liked successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/review")
    public ResponseEntity<RestResponse<Void>> unlikeReview(@RequestParam Long userId,
            @RequestParam Long reviewId) {
        likeService.unlikeReview(userId, reviewId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review unliked successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/discussion")
    public ResponseEntity<RestResponse<Void>> likeDiscussion(@RequestParam Long userId,
            @RequestParam Long discussionId) {
        likeService.likeDiscussion(userId, discussionId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion liked successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/discussion")
    public ResponseEntity<RestResponse<Void>> unlikeDiscussion(@RequestParam Long userId,
            @RequestParam Long discussionId) {
        likeService.unlikeDiscussion(userId, discussionId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion unliked successfully");
        return ResponseEntity.ok(response);
    }
}