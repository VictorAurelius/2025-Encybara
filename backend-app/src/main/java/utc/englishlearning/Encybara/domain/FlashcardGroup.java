package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "flashcard_groups")
@Getter
@Setter
public class FlashcardGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @OneToMany(mappedBy = "flashcardGroup", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Flashcard> flashcards;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;
}