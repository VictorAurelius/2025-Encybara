package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.service.UserEnglishLevelService;
import utc.englishlearning.Encybara.service.UserEnglishLevelService.EnglishLevelDTO;
import utc.englishlearning.Encybara.util.constant.EnglishLevelEnum;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-english-level")
public class UserEnglishLevelController {

    @Autowired
    private UserEnglishLevelService userEnglishLevelService;

    @PutMapping("/{userId}")
    public ResponseEntity<RestResponse<Void>> setUserEnglishLevel(
            @PathVariable("userId") Long userId,
            @RequestParam("level") String level) {
        userEnglishLevelService.setUserEnglishLevel(userId, EnglishLevelEnum.valueOf(level.toUpperCase()));

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("User English level set successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<RestResponse<EnglishLevelDTO>> getUserEnglishLevel(
            @PathVariable("userId") Long userId) {
        EnglishLevelDTO level = userEnglishLevelService.getUserEnglishLevel(userId);

        RestResponse<EnglishLevelDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("User English level retrieved successfully");
        response.setData(level);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/levels")
    public ResponseEntity<RestResponse<List<EnglishLevelDTO>>> getAllEnglishLevels() {
        List<EnglishLevelDTO> levels = userEnglishLevelService.getAllEnglishLevels();

        RestResponse<List<EnglishLevelDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("English levels retrieved successfully");
        response.setData(levels);
        return ResponseEntity.ok(response);
    }
}