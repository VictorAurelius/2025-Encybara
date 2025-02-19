package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    Page<Enrollment> findByUserIdAndProStatus(Long userId, Boolean proStatus, Pageable pageable);
}