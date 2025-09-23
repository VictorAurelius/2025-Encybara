package utc.englishlearning.Encybara.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.ReviewStatusEnum;

@Entity
@Table(name = "reviews")
@Getter
@Setter

public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String reContent;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String reSubject;
    private ReviewStatusEnum status;
    private int numLike;
    private int numStar;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
}
