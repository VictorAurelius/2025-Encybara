package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.enrollment.*;
import utc.englishlearning.Encybara.domain.response.enrollment.*;
import utc.englishlearning.Encybara.domain.response.enrollment.ResEnrollmentWithRecommendationsDTO.CourseRecommendation;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.service.EnrollmentService;
import java.util.List;

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

    /**
     * Step 1: Save completion info
     */
    @PostMapping("/{id}/save-completion")
    public ResponseEntity<RestResponse<ResEnrollmentDTO>> saveCompletion(@PathVariable("id") Long enrollmentId) {
        ResEnrollmentDTO result = enrollmentService.saveEnrollmentCompletion(enrollmentId);
        RestResponse<ResEnrollmentDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Completion info saved successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 2: Update learning results
     */
    @PostMapping("/{id}/update-learning")
    public ResponseEntity<RestResponse<Void>> updateLearningResults(@PathVariable("id") Long enrollmentId) {
        enrollmentService.updateLearningResults(enrollmentId);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning results updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Step 3: Get recommendations
     */
    @PostMapping("/{id}/recommendations")
    public ResponseEntity<RestResponse<List<CourseRecommendation>>> getRecommendations(
            @PathVariable("id") Long enrollmentId) {
        List<CourseRecommendation> recommendations = enrollmentService.createRecommendations(enrollmentId);
        RestResponse<List<CourseRecommendation>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course recommendations created successfully");
        response.setData(recommendations);
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