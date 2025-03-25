package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.RestResponse;
import utc.englishlearning.Encybara.domain.request.lesson.ReqCreateLessonResultDTO;
import utc.englishlearning.Encybara.domain.response.lesson.ResLessonResultDTO;
import utc.englishlearning.Encybara.service.LessonResultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-results")
public class LessonResultController {

    @Autowired
    private LessonResultService lessonResultService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<RestResponse<ResLessonResultDTO>> createLessonResultWithUserId(
            @RequestBody ReqCreateLessonResultDTO reqDto, @PathVariable("userId") Long userId) {
        ResLessonResultDTO createdResult = lessonResultService.createLessonResultWithUserId(reqDto, userId);
        RestResponse<ResLessonResultDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Lesson result created successfully with user ID");
        response.setData(createdResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<RestResponse<Page<ResLessonResultDTO>>> getResultsByLessonId(
            @PathVariable("lessonId") Long lessonId, Pageable pageable) {
        Page<ResLessonResultDTO> results = lessonResultService.getResultsByLessonIdAsDTO(lessonId, pageable);
        RestResponse<Page<ResLessonResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/lesson/{lessonId}")
    public ResponseEntity<RestResponse<Page<ResLessonResultDTO>>> getResultsByUserIdAndLessonId(
            @PathVariable("userId") Long userId, @PathVariable("lessonId") Long lessonId, Pageable pageable) {
        Page<ResLessonResultDTO> results = lessonResultService.getResultsByUserIdAndLessonIdAsDTO(userId, lessonId,
                pageable);
        RestResponse<Page<ResLessonResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<RestResponse<Page<ResLessonResultDTO>>> getLatestResultsByUserId(
            @PathVariable("userId") Long userId, Pageable pageable) {
        Page<ResLessonResultDTO> results = lessonResultService.getLatestResultsByUserIdAsDTO(userId, pageable);
        RestResponse<Page<ResLessonResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Latest results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/lesson/{lessonId}/latest")
    public ResponseEntity<RestResponse<List<ResLessonResultDTO>>> getLatestResultsByUserIdAndLessonId(
            @PathVariable("userId") Long userId, @PathVariable("lessonId") Long lessonId) {
        List<ResLessonResultDTO> results = lessonResultService.getLatestResultsByUserIdAndLessonIdAsDTO(userId,
                lessonId);
        RestResponse<List<ResLessonResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Latest results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }
}