package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByName(String name);

    Page<Course> findByGroup(String group, Pageable pageable); // Support pagination for group query
}