package utc.englishlearning.Encybara.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import utc.englishlearning.Encybara.domain.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>, JpaSpecificationExecutor<Admin> {
    Admin findByEmail(String email);

    boolean existsByEmail(String email);

    Admin findByRefreshTokenAndEmail(String token, String email);
}
