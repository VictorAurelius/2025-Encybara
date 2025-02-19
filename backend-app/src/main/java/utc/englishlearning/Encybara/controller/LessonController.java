package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.lesson.ReqAddQuestionsToLessonDTO;
import utc.englishlearning.Encybara.domain.request.lesson.ReqCreateLessonDTO;
import utc.englishlearning.Encybara.domain.request.lesson.ReqUpdateLessonDTO;
import utc.englishlearning.Encybara.domain.request.lesson.ReqRemoveQuestionFromLessonDTO;
import utc.englishlearning.Encybara.domain.response.lesson.ResLessonDTO;
import utc.englishlearning.Encybara.service.LessonService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @PostMapping
    public ResponseEntity<RestResponse<ResLessonDTO>> createLesson(@RequestBody ReqCreateLessonDTO reqCreateLessonDTO) {
        ResLessonDTO lessonDTO = lessonService.createLesson(reqCreateLessonDTO);
        RestResponse<ResLessonDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lesson created successfully");
        response.setData(lessonDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<ResLessonDTO>> updateLesson(@PathVariable("id") Long id,
            @RequestBody ReqUpdateLessonDTO reqUpdateLessonDTO) {
        ResLessonDTO lessonDTO = lessonService.updateLesson(id, reqUpdateLessonDTO);
        RestResponse<ResLessonDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lesson updated successfully");
        response.setData(lessonDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResLessonDTO>> getLessonById(@PathVariable("id") Long id) {
        ResLessonDTO lessonDTO = lessonService.getLessonById(id);
        RestResponse<ResLessonDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lesson retrieved successfully");
        response.setData(lessonDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RestResponse<Page<ResLessonDTO>>> getAllLessons(Pageable pageable) {
        Page<ResLessonDTO> lessons = lessonService.getAllLessons(pageable);
        RestResponse<Page<ResLessonDTO>> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lessons retrieved successfully");
        response.setData(lessons);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{lessonId}/questions")
    public ResponseEntity<RestResponse<Void>> addQuestionsToLesson(@PathVariable("lessonId") Long lessonId,
            @RequestBody ReqAddQuestionsToLessonDTO reqAddQuestionsToLessonDTO) {
        lessonService.addQuestionsToLesson(lessonId, reqAddQuestionsToLessonDTO);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Questions added to lesson successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lessonId}/questions")
    public ResponseEntity<RestResponse<Void>> removeQuestionFromLesson(@PathVariable("lessonId") Long lessonId,
            @RequestBody ReqRemoveQuestionFromLessonDTO reqRemoveQuestionFromLessonDTO) {
        lessonService.removeQuestionFromLesson(lessonId, reqRemoveQuestionFromLessonDTO.getQuestionId());
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Question removed from lesson successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteLesson(@PathVariable("id") Long id) {
        lessonService.deleteLesson(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lesson deleted successfully");
        return ResponseEntity.ok(response);
    }
}