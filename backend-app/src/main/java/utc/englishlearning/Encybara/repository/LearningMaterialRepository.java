package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import utc.englishlearning.Encybara.domain.Learning_Material;
import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.domain.Question;

import java.util.List;

public interface LearningMaterialRepository extends JpaRepository<Learning_Material, Long> {

    // Thêm phương thức để lấy tất cả tài liệu học tập theo lessonId
    List<Learning_Material> findByLesson(Lesson lesson);

    // Thêm phương thức để lấy tất cả tài liệu học tập theo questionId
    List<Learning_Material> findByQuestion(Question question);
}