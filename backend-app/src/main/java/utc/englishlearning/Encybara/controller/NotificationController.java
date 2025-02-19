package utc.englishlearning.Encybara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utc.englishlearning.Encybara.service.NotificationService;
import utc.englishlearning.Encybara.domain.Notification;
import utc.englishlearning.Encybara.domain.request.notification.ReqNotificationDTO;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.notification.ResNotificationDTO;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<RestResponse<ResNotificationDTO>> createNotification(
            @RequestBody ReqNotificationDTO requestDTO) {
        Notification notification = notificationService.createNotification(requestDTO);
        ResNotificationDTO responseDTO = new ResNotificationDTO();
        responseDTO.setId(notification.getId());
        responseDTO.setMessage(notification.getMessage());
        responseDTO.setRead(notification.isRead());
        responseDTO.setUserId(notification.getUserId());
        responseDTO.setCreatedAt(notification.getCreatedAt());

        RestResponse<ResNotificationDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Notification created successfully");
        response.setData(responseDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<Page<ResNotificationDTO>>> getAllNotificationsByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getAllNotificationsByUserId(userId, pageable);

        RestResponse<Page<ResNotificationDTO>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Notifications retrieved successfully");
        response.setData(notifications.map(notification -> {
            ResNotificationDTO dto = new ResNotificationDTO();
            dto.setId(notification.getId());
            dto.setMessage(notification.getMessage());
            dto.setRead(notification.isRead());
            dto.setUserId(notification.getUserId());
            dto.setCreatedAt(notification.getCreatedAt());
            return dto;
        }));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/read/{notificationId}")
    public ResponseEntity<RestResponse<ResNotificationDTO>> markAsRead(
            @PathVariable("notificationId") Long notificationId) {
        Notification notification = notificationService.markAsRead(notificationId);
        ResNotificationDTO responseDTO = new ResNotificationDTO();
        responseDTO.setId(notification.getId());
        responseDTO.setMessage(notification.getMessage());
        responseDTO.setRead(notification.isRead());
        responseDTO.setUserId(notification.getUserId());
        responseDTO.setCreatedAt(notification.getCreatedAt());

        RestResponse<ResNotificationDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Notification marked as read successfully");
        response.setData(responseDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<RestResponse<String>> deleteNotification(
            @PathVariable("notificationId") Long notificationId) {
        notificationService.deleteNotification(notificationId);
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Notification deleted successfully");
        response.setData("Notification with ID " + notificationId + " has been deleted.");
        return ResponseEntity.ok(response);
    }
}