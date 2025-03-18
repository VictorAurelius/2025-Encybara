package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByName(String name);

    List<Course> findByGroup(String group); // JPA will automatically map this to the course_group column name
}