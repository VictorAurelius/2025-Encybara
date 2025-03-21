package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.service.InitialAssessmentService;
import utc.englishlearning.Encybara.domain.response.RestResponse;

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
     * Handles users who choose to take the initial assessment.
     * Creates an enrollment for the assessment course (ID: 1) but no learning
     * result yet.
     * Learning result will be created after assessment completion.
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
}