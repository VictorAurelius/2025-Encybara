package utc.englishlearning.Encybara.domain.request.answer;

public class ReqCreateAnswerDTO {
    private Long questionId;
    private String answerContent;
    private Integer pointAchieved;
    private String improvement;
    private Long enrollmentId; // Added enrollment ID

    // Getters and Setters
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