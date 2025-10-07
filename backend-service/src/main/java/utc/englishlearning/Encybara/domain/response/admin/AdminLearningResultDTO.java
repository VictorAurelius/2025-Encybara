package utc.englishlearning.Encybara.domain.response.admin;

import java.time.Instant;

public class AdminLearningResultDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String englishLevel;
    private Double listeningScore;
    private Double speakingScore;
    private Double readingScore;
    private Double writingScore;
    private Double previousListeningScore;
    private Double previousSpeakingScore;
    private Double previousReadingScore;
    private Double previousWritingScore;
    private Instant lastUpdated;

    // Progress calculations
    private Double listeningProgress;
    private Double speakingProgress;
    private Double readingProgress;
    private Double writingProgress;
    private Double overallProgress;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEnglishLevel() {
        return englishLevel;
    }

    public void setEnglishLevel(String englishLevel) {
        this.englishLevel = englishLevel;
    }

    public Double getListeningScore() {
        return listeningScore;
    }

    public void setListeningScore(Double listeningScore) {
        this.listeningScore = listeningScore;
    }

    public Double getSpeakingScore() {
        return speakingScore;
    }

    public void setSpeakingScore(Double speakingScore) {
        this.speakingScore = speakingScore;
    }

    public Double getReadingScore() {
        return readingScore;
    }

    public void setReadingScore(Double readingScore) {
        this.readingScore = readingScore;
    }

    public Double getWritingScore() {
        return writingScore;
    }

    public void setWritingScore(Double writingScore) {
        this.writingScore = writingScore;
    }

    public Double getPreviousListeningScore() {
        return previousListeningScore;
    }

    public void setPreviousListeningScore(Double previousListeningScore) {
        this.previousListeningScore = previousListeningScore;
    }

    public Double getPreviousSpeakingScore() {
        return previousSpeakingScore;
    }

    public void setPreviousSpeakingScore(Double previousSpeakingScore) {
        this.previousSpeakingScore = previousSpeakingScore;
    }

    public Double getPreviousReadingScore() {
        return previousReadingScore;
    }

    public void setPreviousReadingScore(Double previousReadingScore) {
        this.previousReadingScore = previousReadingScore;
    }

    public Double getPreviousWritingScore() {
        return previousWritingScore;
    }

    public void setPreviousWritingScore(Double previousWritingScore) {
        this.previousWritingScore = previousWritingScore;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Double getListeningProgress() {
        return listeningProgress;
    }

    public void setListeningProgress(Double listeningProgress) {
        this.listeningProgress = listeningProgress;
    }

    public Double getSpeakingProgress() {
        return speakingProgress;
    }

    public void setSpeakingProgress(Double speakingProgress) {
        this.speakingProgress = speakingProgress;
    }

    public Double getReadingProgress() {
        return readingProgress;
    }

    public void setReadingProgress(Double readingProgress) {
        this.readingProgress = readingProgress;
    }

    public Double getWritingProgress() {
        return writingProgress;
    }

    public void setWritingProgress(Double writingProgress) {
        this.writingProgress = writingProgress;
    }

    public Double getOverallProgress() {
        return overallProgress;
    }

    public void setOverallProgress(Double overallProgress) {
        this.overallProgress = overallProgress;
    }
}