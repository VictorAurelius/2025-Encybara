package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utc.englishlearning.Encybara.domain.Lesson_Result;
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

    // @PostMapping
    // public ResponseEntity<RestResponse<ResLessonResultDTO>> createLessonResult(
    // @RequestBody ReqCreateLessonResultDTO reqDto) {
    // ResLessonResultDTO result = lessonResultService.createLessonResult(reqDto);
    // RestResponse<ResLessonResultDTO> response = new RestResponse<>();
    // response.setStatusCode(200);
    // response.setMessage("Lesson result created successfully");
    // response.setData(result);
    // return ResponseEntity.ok(response);
    // }

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
    public ResponseEntity<RestResponse<Page<Lesson_Result>>> getResultsByLessonId(
            @PathVariable("lessonId") Long lessonId, Pageable pageable) {
        Page<Lesson_Result> results = lessonResultService.getResultsByLessonId(lessonId, pageable);
        RestResponse<Page<Lesson_Result>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/lesson/{lessonId}")
    public ResponseEntity<RestResponse<Page<Lesson_Result>>> getResultsByUserIdAndLessonId(
            @PathVariable("userId") Long userId, @PathVariable("lessonId") Long lessonId, Pageable pageable) {
        Page<Lesson_Result> results = lessonResultService.getResultsByUserIdAndLessonId(userId, lessonId, pageable);
        RestResponse<Page<Lesson_Result>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<RestResponse<Page<Lesson_Result>>> getLatestResultsByUserId(
            @PathVariable("userId") Long userId, Pageable pageable) {
        Page<Lesson_Result> results = lessonResultService.getLatestResultsByUserId(userId, pageable);
        RestResponse<Page<Lesson_Result>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Latest results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/lesson/{lessonId}/latest")
    public ResponseEntity<RestResponse<List<Lesson_Result>>> getLatestResultsByUserIdAndLessonId(
            @PathVariable("userId") Long userId, @PathVariable("lessonId") Long lessonId) {
        List<Lesson_Result> results = lessonResultService.getLatestResultsByUserIdAndLessonId(userId, lessonId);
        RestResponse<List<Lesson_Result>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Latest results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }
}