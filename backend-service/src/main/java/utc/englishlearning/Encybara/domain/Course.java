package utc.englishlearning.Encybara.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.SecurityUtil;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Transient;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String name;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String intro;
    private double diffLevel;
    private double recomLevel;
    private CourseTypeEnum courseType;
    private SpecialFieldEnum speciField;
    private int numLike;
    private String createBy;
    private Instant createAt;
    private String updateBy;
    private Instant updateAt;
    private CourseStatusEnum courseStatus;
    private Integer sumLesson;
    @Column(name = "course_group") // Changed from 'group' to avoid SQL reserved keyword
    private String group;

    @Transient // This field won't be persisted to database
    private List<String> lessonNames = new ArrayList<>();

    @PrePersist
    public void handleBeforeCreate() {
        this.createBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.createAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateBy = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.updateAt = Instant.now();
    }

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Course_Lesson> courselessons = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    public List<Lesson> getLessons() {
        List<Lesson> lessons = new ArrayList<>();
        if (courselessons != null) {
            for (Course_Lesson courseLesson : courselessons) {
                if (courseLesson != null && courseLesson.getLesson() != null) {
                    lessons.add(courseLesson.getLesson());
                }
            }
        }
        return lessons;
    }
}
