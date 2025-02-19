package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Notification;
import utc.englishlearning.Encybara.domain.request.notification.ReqNotificationDTO;
import utc.englishlearning.Encybara.exception.NotificationNotFoundException;
import utc.englishlearning.Encybara.repository.NotificationRepository;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(ReqNotificationDTO requestDTO) {
        Notification notification = new Notification();
        notification.setMessage(requestDTO.getMessage());
        notification.setUserId(requestDTO.getUserId());
        notification.setRead(false); // Mặc định là chưa đọc
        notification.setCreatedAt(Instant.now()); // Thay đổi để sử dụng Instant
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotificationsByUserId(Long userId) {
        return notificationRepository.findAll().stream()
                .filter(notification -> notification.getUserId().equals(userId))
                .toList();
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public Page<Notification> getAllNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findAllByUserId(userId, pageable);
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
    }
}