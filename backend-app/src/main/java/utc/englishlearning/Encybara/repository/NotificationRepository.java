package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import utc.englishlearning.Encybara.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Có thể thêm các phương thức truy vấn tùy chỉnh nếu cần
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);
}