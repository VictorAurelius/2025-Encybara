package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.enrollment.ReqCreateEnrollmentDTO;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentDTO;
import utc.englishlearning.Encybara.service.EnrollmentService;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<RestResponse<Void>> joinCourse(@PathVariable Long id) {
        enrollmentService.joinCourse(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Joined course successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> refuseCourse(@PathVariable Long id) {
        enrollmentService.refuseCourse(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Enrollment deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<Page<ResEnrollmentDTO>>> getEnrollmentsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean proStatus,
            Pageable pageable) {
        Page<ResEnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByUserId(userId, proStatus, pageable);
        RestResponse<Page<ResEnrollmentDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Enrollments retrieved successfully");
        response.setData(enrollments);
        return ResponseEntity.ok(response);
    }
}