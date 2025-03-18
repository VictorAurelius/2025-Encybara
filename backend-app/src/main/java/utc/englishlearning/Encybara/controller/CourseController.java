package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utc.englishlearning.Encybara.domain.request.course.ReqCreateCourseDTO;
import utc.englishlearning.Encybara.domain.request.course.ReqUpdateCourseDTO;
import utc.englishlearning.Encybara.domain.response.course.ResCourseDTO;
import utc.englishlearning.Encybara.service.CourseService;
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

    @GetMapping("/group/{group}")
    public ResponseEntity<RestResponse<Page<ResCourseDTO>>> getCoursesByGroup(
            @PathVariable("group") String group,
            Pageable pageable) {
        Page<ResCourseDTO> courses = courseService.getCoursesByGroup(group, pageable);
        RestResponse<Page<ResCourseDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Courses retrieved successfully by group");
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

    @PutMapping("/{id}/publish")
    public ResponseEntity<RestResponse<Void>> publishCourse(@PathVariable("id") Long id) {
        courseService.publishCourse(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Course has been published successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/make-private")
    public ResponseEntity<RestResponse<Void>> makePrivate(@PathVariable("id") Long id) {
        courseService.makePrivate(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Course has been made private successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/make-public")
    public ResponseEntity<RestResponse<Void>> makePublic(@PathVariable("id") Long id) {
        courseService.makePublic(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Course has been made public successfully");
        return ResponseEntity.ok(response);
    }
}