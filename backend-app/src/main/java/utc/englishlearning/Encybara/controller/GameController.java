package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.GameQuestion;
import utc.englishlearning.Encybara.domain.GameSession;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.service.GameService;
import utc.englishlearning.Encybara.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    @PostMapping("/start")
    public ResponseEntity<?> startGame(Authentication authentication) {
        try {
            User user = userService.handleGetUserByUsername(authentication.getName());
            GameSession session = gameService.startGame(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("timeLeft", session.getTimeLeft());
            response.put("endTime", session.getStartTime().plusSeconds(45));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}/question")
    public ResponseEntity<?> getNextQuestion(@PathVariable Long sessionId) {
        try {
            GameQuestion question = gameService.getNextQuestion(sessionId);
            if (question == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("questionId", question.getId());
            response.put("text", question.getText());
            response.put("choices", question.getChoices());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<?> submitAnswer(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {
        try {
            String answer = request.get("answer");
            if (answer == null || answer.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Answer is required");
            }

            Map<String, Object> result = gameService.submitAnswer(sessionId, answer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endGame(@PathVariable Long sessionId) {
        try {
            gameService.endGame(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "Game ended successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(Authentication authentication) {
        try {
            User user = userService.handleGetUserByUsername(authentication.getName());
            Map<String, Object> leaderboard = gameService.getLeaderboard(user.getId());
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{sessionId}/time")
    public ResponseEntity<?> updateTimeLeft(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer timeLeft = request.get("timeLeft");
            if (timeLeft == null) {
                return ResponseEntity.badRequest().body("Time left is required");
            }

            gameService.updateTimeLeft(sessionId, timeLeft);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}