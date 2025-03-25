package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.enrollment.ReqCreateEnrollmentDTO;
import utc.englishlearning.Encybara.domain.request.enrollment.ReqCalculateEnrollmentResultDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentWithRecommendationsDTO;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.service.EnrollmentService;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<RestResponse<ResEnrollmentDTO>> createEnrollment(
            @RequestBody ReqCreateEnrollmentDTO reqCreateEnrollmentDTO) {
        ResEnrollmentDTO enrollmentDTO = enrollmentService.createEnrollment(reqCreateEnrollmentDTO);
        RestResponse<ResEnrollmentDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Enrollment created successfully");
        response.setData(enrollmentDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/join")
    public ResponseEntity<RestResponse<Void>> joinCourse(@PathVariable("id") Long id) {
        enrollmentService.joinCourse(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course joined successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> refuseCourse(@PathVariable("id") Long id) {
        enrollmentService.refuseCourse(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course refused successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<Page<ResEnrollmentDTO>>> getEnrollmentsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "proStatus", required = false) Boolean proStatus,
            Pageable pageable) {
        Page<ResEnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByUserId(userId, proStatus, pageable);
        RestResponse<Page<ResEnrollmentDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Enrollments retrieved successfully");
        response.setData(enrollments);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate-result")
    public ResponseEntity<RestResponse<ResEnrollmentWithRecommendationsDTO>> calculateEnrollmentResult(
            @RequestBody ReqCalculateEnrollmentResultDTO reqDto) {
        ResEnrollmentWithRecommendationsDTO result = enrollmentService.calculateEnrollmentResult(reqDto);
        RestResponse<ResEnrollmentWithRecommendationsDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Enrollment result calculated successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<RestResponse<ResEnrollmentDTO>> getLatestEnrollment(
            @RequestParam("courseId") Long courseId,
            @RequestParam("userId") Long userId) {
        ResEnrollmentDTO enrollmentDTO = enrollmentService.getLatestEnrollmentByCourseIdAndUserId(courseId, userId);
        RestResponse<ResEnrollmentDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Latest enrollment retrieved successfully");
        response.setData(enrollmentDTO);
        return ResponseEntity.ok(response);
    }
}