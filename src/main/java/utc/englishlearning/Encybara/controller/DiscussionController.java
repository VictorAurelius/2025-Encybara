package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.discussion.ReqCreateDiscussionDTO;
import utc.englishlearning.Encybara.domain.response.discussion.ResDiscussionDTO;
import utc.englishlearning.Encybara.service.DiscussionService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

@RestController
@RequestMapping("/api/v1/discussions")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @PostMapping
    public ResponseEntity<RestResponse<ResDiscussionDTO>> createDiscussion(
            @RequestBody ReqCreateDiscussionDTO reqCreateDiscussionDTO) {
        ResDiscussionDTO discussionDTO = discussionService.createDiscussion(reqCreateDiscussionDTO);
        RestResponse<ResDiscussionDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion created successfully");
        response.setData(discussionDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteDiscussion(@PathVariable Long id) {
        discussionService.deleteDiscussion(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResDiscussionDTO>> getDiscussionById(@PathVariable Long id) {
        ResDiscussionDTO discussionDTO = discussionService.getDiscussionById(id);
        RestResponse<ResDiscussionDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussion retrieved successfully");
        response.setData(discussionDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<Page<ResDiscussionDTO>>> getAllDiscussionsByUserId(
            @PathVariable Long userId, Pageable pageable) {
        Page<ResDiscussionDTO> discussions = discussionService.getAllDiscussionsByUserId(userId, pageable);
        RestResponse<Page<ResDiscussionDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussions retrieved successfully");
        response.setData(discussions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<RestResponse<Page<ResDiscussionDTO>>> getAllDiscussionsByLessonId(
            @PathVariable Long lessonId, Pageable pageable) {
        Page<ResDiscussionDTO> discussions = discussionService.getAllDiscussionsByLessonId(lessonId, pageable);
        RestResponse<Page<ResDiscussionDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Discussions retrieved successfully");
        response.setData(discussions);
        return ResponseEntity.ok(response);
    }
}