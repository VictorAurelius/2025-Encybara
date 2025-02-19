package utc.englishlearning.Encybara.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.SecurityUtil;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String intro;
    private int diffLevel;
    private int recomLevel;
    private CourseTypeEnum courseType;
    private SpecialFieldEnum speciField;
    private int numLike;
    private String createBy;
    private Instant createAt;
    private String updateBy;
    private Instant updateAt;

    private Integer sumLesson;

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
    List<Course_Lesson> courselessons;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @JsonIgnore
    List<Enrollment> enrollments;

    public List<Lesson> getLessons() {
        List<Lesson> lessons = new ArrayList<>();
        for (Course_Lesson courseLesson : courselessons) {
            lessons.add(courseLesson.getLesson());
        }
        return lessons;
    }
}
