package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.request.assessment.ReqCompletePlacementDTO;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.service.InitialAssessmentService;
import jakarta.validation.Valid;

/**
 * Controller for handling initial assessment choices when users register.
 * Provides endpoints for either skipping the assessment or starting it.
 */
@RestController
@RequestMapping("/api/v1/initial-assessment")
public class InitialAssessmentController {

    @Autowired
    private InitialAssessmentService initialAssessmentService;

    /**
     * Handles users who choose to skip the initial assessment.
     * Sets base scores (1.0) for all skills and creates course recommendations.
     *
     * @param userId The ID of the user who is skipping assessment
     * @return Response indicating successful creation of base scores and
     *         recommendations
     */
    @PostMapping("/{userId}/skip")
    public ResponseEntity<RestResponse<Void>> skipInitialAssessment(
            @PathVariable("userId") Long userId) {
        initialAssessmentService.skipInitialAssessment(userId);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Initial assessment skipped and base recommendations created");
        return ResponseEntity.ok(response);
    }

    /**
     * Creates enrollment for placement test course.
     *
     * @param userId The ID of the user starting the assessment
     * @return Response indicating successful enrollment in assessment course
     */
    @PostMapping("/{userId}/start")
    public ResponseEntity<RestResponse<Void>> startInitialAssessment(
            @PathVariable("userId") Long userId) {
        initialAssessmentService.startInitialAssessment(userId);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Initial assessment course enrollment created");
        return ResponseEntity.ok(response);
    }

    /**
     * Completes the placement assessment and creates course recommendations based
     * on results.
     *
     * @param request The DTO containing completion data including enrollmentId,
     *                comLevel, and totalPoints
     * @return Response indicating successful completion and recommendation creation
     */
    @PostMapping("/complete")
    public ResponseEntity<RestResponse<Void>> completePlacementAssessment(
            @Valid @RequestBody ReqCompletePlacementDTO request) {
        initialAssessmentService.completePlacementAssessment(request);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Placement assessment completed and recommendations created");
        return ResponseEntity.ok(response);
    }
}