package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "likes")
@Getter
@Setter
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = true)
    private Review review;

    @ManyToOne
    @JoinColumn(name = "discussion_id", nullable = true)
    private Discussion discussion;
}