package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.admin.AdminLearningResultDTO;
import utc.englishlearning.Encybara.service.AdminLearningResultService;

@RestController
@RequestMapping("/api/v1/admin/learning-results")
public class AdminLearningResultController {

    @Autowired
    private AdminLearningResultService adminLearningResultService;

    @GetMapping
    public ResponseEntity<RestResponse<Page<AdminLearningResultDTO>>> getAllLearningResults(Pageable pageable) {
        Page<AdminLearningResultDTO> results = adminLearningResultService.getAllLearningResults(pageable);

        RestResponse<Page<AdminLearningResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning results retrieved successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<AdminLearningResultDTO>> getLearningResultById(@PathVariable Long id) {
        AdminLearningResultDTO result = adminLearningResultService.getLearningResultById(id);

        RestResponse<AdminLearningResultDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning result retrieved successfully");
        response.setData(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<RestResponse<Page<AdminLearningResultDTO>>> searchLearningResults(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String userName,
            Pageable pageable) {

        Page<AdminLearningResultDTO> results = adminLearningResultService.searchLearningResults(
                userEmail, userName, pageable);

        RestResponse<Page<AdminLearningResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning results search completed successfully");
        response.setData(results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-level/{level}")
    public ResponseEntity<RestResponse<Page<AdminLearningResultDTO>>> getLearningResultsByLevel(
            @PathVariable String level,
            Pageable pageable) {

        Page<AdminLearningResultDTO> results = adminLearningResultService.getLearningResultsByLevel(level, pageable);

        RestResponse<Page<AdminLearningResultDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning results retrieved successfully for level: " + level);
        response.setData(results);
        return ResponseEntity.ok(response);
    }
}