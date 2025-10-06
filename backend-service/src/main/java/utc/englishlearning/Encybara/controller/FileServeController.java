package utc.englishlearning.Encybara.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utc.englishlearning.Encybara.service.LearningMaterialService;

@RestController
@RequestMapping("/uploadfile")
public class FileServeController {
    private static final Logger logger = LoggerFactory.getLogger(FileServeController.class);
    private final LearningMaterialService learningMaterialService;

    public FileServeController(LearningMaterialService learningMaterialService) {
        this.learningMaterialService = learningMaterialService;
    }

    @GetMapping("/health")
    public ResponseEntity<RestResponse<String>> healthCheck() {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("File server is running");
        response.setData(learningMaterialService.getServerUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{*filePath}")
    public ResponseEntity<?> serveFile(@PathVariable("filePath") String filePath) {
        try {
            logger.debug("Attempting to serve file: {}", filePath);

            // Security check for path traversal attempts
            if (filePath.contains("..")) {
                logger.warn("Path traversal attempt detected: {}", filePath);
                RestResponse<Void> response = new RestResponse<>();
                response.setStatusCode(400);
                response.setMessage("Invalid file path");
                return ResponseEntity.badRequest().body(response);
            }

            String fullPath = learningMaterialService.buildResourcePath(filePath);
            logger.debug("Resolved full path: {}", fullPath);
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

            InputStreamResource resource = learningMaterialService.getResource(fullPath);

            HttpHeaders headers = new HttpHeaders();
            String contentType = determineContentType(fileName);
            headers.setContentType(MediaType.parseMediaType(contentType));

            if (contentType.startsWith("image/") || contentType.startsWith("video/")
                    || contentType.startsWith("audio/")) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            } else {
                headers.setContentDispositionFormData("attachment", fileName);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (FileNotFoundException e) {
            logger.warn("File not found: {} - {}", filePath, e.getMessage());
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(404);
            response.setMessage("File not found: " + filePath);
            return ResponseEntity.status(404).body(response);
        } catch (IOException e) {
            logger.error("IO error while serving file: {} - {}", filePath, e.getMessage());
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(500);
            response.setMessage("Error reading file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error while serving file: {}", filePath, e);
            RestResponse<Void> response = new RestResponse<>();
            response.setStatusCode(500);
            response.setMessage("Internal server error");
            return ResponseEntity.status(500).body(response);
        }
    }

    private String determineContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "mp3" -> "audio/mpeg";
            case "mp4" -> "video/mp4";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}