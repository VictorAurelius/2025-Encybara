package utc.englishlearning.Encybara.domain.response.answer;

public class ResAnswerDTO {
    private Long id;
    private Long questionId;
    private String answerContent;
    private Integer pointAchieved;
    private Long sessionId;
    private String improvement;
    private Long enrollmentId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerContent() {
        return answerContent;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public Integer getPointAchieved() {
        return pointAchieved;
    }

    public void setPointAchieved(Integer pointAchieved) {
        this.pointAchieved = pointAchieved;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getImprovement() {
        return improvement;
    }

    public void setImprovement(String improvement) {
        this.improvement = improvement;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }
}