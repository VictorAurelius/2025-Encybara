package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.domain.Learning_Material;
import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.domain.Question;

import java.util.List;

public interface LearningMaterialRepository extends JpaRepository<Learning_Material, Long> {

    // Thêm phương thức để lấy tất cả tài liệu học tập theo lessonId
    List<Learning_Material> findByLesson(Lesson lesson);

    // Thêm phương thức để lấy tất cả tài liệu học tập theo questionId
    List<Learning_Material> findByQuestion(Question question);

    List<Learning_Material> findByCourse(Course course);

    // Phương thức để lấy tất cả courseId có learning materials
    @Query("SELECT DISTINCT lm.course.id FROM Learning_Material lm WHERE lm.course IS NOT NULL")
    List<Long> findDistinctCourseIds();

    // Phương thức để lấy thông tin course (id và name) có learning materials
    @Query("SELECT DISTINCT lm.course FROM Learning_Material lm WHERE lm.course IS NOT NULL")
    List<Course> findDistinctCoursesWithMaterials();
}