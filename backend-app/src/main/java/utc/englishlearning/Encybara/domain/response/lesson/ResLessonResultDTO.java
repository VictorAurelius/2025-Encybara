package utc.englishlearning.Encybara.domain.response.lesson;

public class ResLessonResultDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long lessonId;
    private String lessonName;
    private Long enrollmentId;
    private long stuTime; // Changed to long to match the entity
    private Integer totalPoints;
    private Double comLevel;
    private String courseType;
    private String skillType;
    private Double diffLevel;

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

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public long getStuTime() {
        return stuTime;
    }

    public void setStuTime(long stuTime) {
        this.stuTime = stuTime;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Double getComLevel() {
        return comLevel;
    }

    public void setComLevel(Double comLevel) {
        this.comLevel = comLevel;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getSkillType() {
        return skillType;
    }

    public void setSkillType(String skillType) {
        this.skillType = skillType;
    }

    public Double getDiffLevel() {
        return diffLevel;
    }

    public void setDiffLevel(Double diffLevel) {
        this.diffLevel = diffLevel;
    }
}