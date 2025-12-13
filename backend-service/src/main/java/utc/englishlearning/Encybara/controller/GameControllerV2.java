package utc.englishlearning.Encybara.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.response.GameSessionStatusResponse;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.service.GameService;
import utc.englishlearning.Encybara.service.UserService;
import utc.englishlearning.Encybara.util.constant.GameTypeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
public class GameControllerV2 {

    private final GameService gameService;
    private final UserService userService;

    // === Game Management APIs ===
    
    @PostMapping("/create")
    public ResponseEntity<RestResponse<Game>> createGame(@RequestBody Map<String, Object> request) {
        try {
            Long courseId = Long.valueOf(request.get("courseId").toString());
            String name = request.get("name").toString();
            String description = request.get("description").toString();
            GameTypeEnum gameType = GameTypeEnum.valueOf(request.get("gameType").toString());
            int maxQuestions = Integer.parseInt(request.get("maxQuestions").toString());
            int timeLimit = Integer.parseInt(request.get("timeLimit").toString());
            
            Game game = gameService.createGame(courseId, name, description, gameType, maxQuestions, timeLimit);
            
            RestResponse<Game> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Game created successfully");
            response.setData(game);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Game> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<RestResponse<List<Game>>> getGamesByCourse(@PathVariable Long courseId) {
        try {
            List<Game> games = gameService.getGamesByCourse(courseId);
            
            RestResponse<List<Game>> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Games retrieved successfully");
            response.setData(games);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<List<Game>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // === Game Session APIs ===
    
    @PostMapping("/{gameId}/start")
    public ResponseEntity<RestResponse<Map<String, Object>>> startGame(
            @PathVariable("gameId") Long gameId,
            Authentication authentication) {
        try {
            User user = userService.handleGetUserByUsername(authentication.getName());
            if (user == null) {
                RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(400);
                errorResponse.setError("User not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            GameSession session = gameService.startGame(gameId, user.getId());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("sessionId", session.getId());
            responseData.put("gameId", gameId);
            responseData.put("maxQuestions", session.getGame().getMaxQuestions());
            responseData.put("timeLimit", session.getGame().getTimeLimit());
            
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Game started successfully");
            response.setData(responseData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{sessionId}/question")
    public ResponseEntity<RestResponse<Map<String, Object>>> getNextQuestion(@PathVariable Long sessionId) {
        try {
            Map<String, Object> questionData = gameService.getNextQuestion(sessionId);
            
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(200);
            
            if (questionData.containsKey("gameCompleted")) {
                response.setMessage("Game completed");
            } else {
                response.setMessage("Question retrieved successfully");
            }
            
            response.setData(questionData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<RestResponse<Map<String, Object>>> submitAnswer(
            @PathVariable("sessionId") Long sessionId,
            @RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Long choiceId = Long.valueOf(request.get("choiceId").toString());

            Map<String, Object> result = gameService.submitAnswer(sessionId, questionId, choiceId);
            
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Answer submitted successfully");
            response.setData(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<RestResponse<Map<String, Object>>> endGame(@PathVariable("sessionId") Long sessionId) {
        try {
            gameService.endGame(sessionId);
            GameSession session = gameService.getGameSession(sessionId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("finalScore", session.getScore());
            responseData.put("accuracy", session.getAccuracy());
            responseData.put("totalQuestions", session.getTotalQuestions());
            responseData.put("correctAnswers", session.getCorrectAnswers());
            
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Game ended successfully");
            response.setData(responseData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @PutMapping("/{gameId}")
    public ResponseEntity<RestResponse<Game>> updateGame(@PathVariable("gameId") Long gameId, @RequestBody Map<String, Object> request) {
        try {
            String name = request.get("name").toString();
            String description = request.get("description").toString();
            GameTypeEnum gameType = GameTypeEnum.valueOf(request.get("gameType").toString());
            int maxQuestions = Integer.parseInt(request.get("maxQuestions").toString());
            int timeLimit = Integer.parseInt(request.get("timeLimit").toString());

            Game updated = gameService.updateGame(gameId, name, description, gameType, maxQuestions, timeLimit);

            RestResponse<Game> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Game updated successfully");
            response.setData(updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Game> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    @DeleteMapping("/{gameId}")
    public ResponseEntity<RestResponse<Void>> deleteGame(@PathVariable("gameId") Long gameId) {
        try {
            gameService.deleteGame(gameId);

            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Game deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Void> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<RestResponse<Map<String, Object>>> getLeaderboard(Authentication authentication) {
        try {
            // Kiểm tra xem authentication có tồn tại và có tên người dùng không
            if (authentication == null || authentication.getName() == null) {
                RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(401);
                errorResponse.setError("Unauthorized: No authentication information provided.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            User user = userService.handleGetUserByUsername(authentication.getName());
            if (user == null) {
                RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
                errorResponse.setStatusCode(404);
                errorResponse.setError("User not found for the given authentication.");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Map<String, Object> leaderboard = gameService.getLeaderboard(user.getId());
            
            RestResponse<Map<String, Object>> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Leaderboard retrieved successfully");
            response.setData(leaderboard);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Map<String, Object>> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<RestResponse<GameSessionStatusResponse>> getGameStatus(@PathVariable("sessionId") Long sessionId) {
        try {
            GameSession session = gameService.getGameSession(sessionId);
            GameSessionStatusResponse statusResponse = GameSessionStatusResponse.fromGameSession(session);
            
            RestResponse<GameSessionStatusResponse> response = new RestResponse<>();
            response.setStatusCode(200);
            response.setMessage("Game status retrieved successfully");
            response.setData(statusResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<GameSessionStatusResponse> errorResponse = new RestResponse<>();
            errorResponse.setStatusCode(400);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}