package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.StudySchedule;

import java.util.List;

@Repository
public interface StudyScheduleRepository extends JpaRepository<StudySchedule, Long> {
    List<StudySchedule> findByUserId(Long userId);

    Page<StudySchedule> findByUserId(Long userId, Pageable pageable);

    List<StudySchedule> findByCourseIdAndUserId(Long courseId, Long userId);
}