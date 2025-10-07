package utc.englishlearning.Encybara.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.SecurityUtil;
import utc.englishlearning.Encybara.util.constant.GameTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameTypeEnum gameType;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(nullable = false)
    private int maxQuestions = 10;
    
    @Column(nullable = false)
    private int timeLimit = 300; // seconds (5 minutes)
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    // Audit fields
    private String createBy;
    private Instant createAt;
    private String updateBy;
    private Instant updateAt;
    
    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<GameSession> gameSessions = new ArrayList<>();
    
    @PrePersist
    public void handleBeforeCreate() {
        this.createBy = SecurityUtil.getCurrentUserLogin().isPresent() 
            ? SecurityUtil.getCurrentUserLogin().get() 
            : "";
        this.createAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updateBy = SecurityUtil.getCurrentUserLogin().isPresent() 
            ? SecurityUtil.getCurrentUserLogin().get() 
            : "";
        this.updateAt = Instant.now();
    }
}