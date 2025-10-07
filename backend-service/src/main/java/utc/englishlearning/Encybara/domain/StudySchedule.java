package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "study_schedules")
@Getter
@Setter
public class StudySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant scheduleTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isDaily; // Thuộc tính để phân biệt lịch học hàng ngày hay cho khóa học cụ thể

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // Khóa học liên quan (nếu có)
}