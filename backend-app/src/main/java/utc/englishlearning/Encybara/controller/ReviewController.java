package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.review.ReqCreateReviewDTO;
import utc.englishlearning.Encybara.domain.response.review.ResReviewDTO;
import utc.englishlearning.Encybara.service.ReviewService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import utc.englishlearning.Encybara.util.constant.ReviewStatusEnum;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<RestResponse<ResReviewDTO>> createReview(@RequestBody ReqCreateReviewDTO reqCreateReviewDTO) {
        ResReviewDTO reviewDTO = reviewService.createReview(reqCreateReviewDTO);
        RestResponse<ResReviewDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review created successfully");
        response.setData(reviewDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<ResReviewDTO>> updateReview(@PathVariable("id") Long id,
            @RequestParam Long userId,
            @RequestBody ReqCreateReviewDTO reqUpdateReviewDTO) {
        ResReviewDTO reviewDTO = reviewService.updateReview(id, userId, reqUpdateReviewDTO);
        RestResponse<ResReviewDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review updated successfully");
        response.setData(reviewDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteReview(@PathVariable("id") Long id, @RequestParam Long userId) {
        reviewService.deleteReview(id, userId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Review deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<RestResponse<Page<ResReviewDTO>>> getAllReviewsByCourseId(
            @PathVariable("courseId") Long courseId,
            @RequestParam(required = false) Integer numStar,
            @RequestParam(required = false) ReviewStatusEnum status,
            Pageable pageable) {
        Page<ResReviewDTO> reviews = reviewService.getAllReviewsByCourseId(courseId, pageable, numStar, status);
        RestResponse<Page<ResReviewDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Reviews retrieved successfully");
        response.setData(reviews);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<Page<ResReviewDTO>>> getAllReviewsByUserId(
            @PathVariable("courseId") Long userId, Pageable pageable) {
        Page<ResReviewDTO> reviews = reviewService.getAllReviewsByUserId(userId, pageable);
        RestResponse<Page<ResReviewDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Reviews retrieved successfully");
        response.setData(reviews);
        return ResponseEntity.ok(response);
    }
}