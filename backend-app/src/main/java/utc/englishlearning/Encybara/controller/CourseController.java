package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.course.ReqCreateCourseDTO;
import utc.englishlearning.Encybara.domain.request.course.ReqUpdateCourseDTO;
import utc.englishlearning.Encybara.domain.response.course.ResCourseDTO;
import utc.englishlearning.Encybara.domain.response.lesson.ResLessonDTO;
import utc.englishlearning.Encybara.service.CourseService;
import utc.englishlearning.Encybara.service.LessonService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.request.course.ReqAddLessonsToCourseDTO;
import utc.englishlearning.Encybara.domain.request.course.ReqRemoveLessonFromCourseDTO;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<RestResponse<ResCourseDTO>> createCourse(@RequestBody ReqCreateCourseDTO reqCreateCourseDTO) {
        ResCourseDTO courseDTO = courseService.createCourse(reqCreateCourseDTO);
        RestResponse<ResCourseDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course created successfully");
        response.setData(courseDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<ResCourseDTO>> updateCourse(@PathVariable("id") Long id,
            @RequestBody ReqUpdateCourseDTO reqUpdateCourseDTO) {
        ResCourseDTO courseDTO = courseService.updateCourse(id, reqUpdateCourseDTO);
        RestResponse<ResCourseDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course updated successfully");
        response.setData(courseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<ResCourseDTO>> getCourseById(@PathVariable("id") Long id) {
        ResCourseDTO courseDTO = courseService.getCourseById(id);
        RestResponse<ResCourseDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course retrieved successfully");
        response.setData(courseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RestResponse<Page<ResCourseDTO>>> getAllCourses(Pageable pageable) {
        Page<ResCourseDTO> courses = courseService.getAllCourses(pageable);
        RestResponse<Page<ResCourseDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Courses retrieved successfully");
        response.setData(courses);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{courseId}/lessons")
    public ResponseEntity<RestResponse<Void>> addLessonsToCourse(@PathVariable("courseId") Long courseId,
            @RequestBody ReqAddLessonsToCourseDTO reqAddLessonsToCourseDTO) {
        courseService.addLessonsToCourse(courseId, reqAddLessonsToCourseDTO);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Lessons added to course successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}/lessons")
    public ResponseEntity<RestResponse<Void>> removeLessonFromCourse(@PathVariable("courseId") Long courseId,
            @RequestBody ReqRemoveLessonFromCourseDTO reqRemoveLessonFromCourseDTO) {
        courseService.removeLessonFromCourse(courseId, reqRemoveLessonFromCourseDTO.getLessonId());
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Lesson removed from course successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Course deleted successfully");
        return ResponseEntity.ok(response);
    }
}