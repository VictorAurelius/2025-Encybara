package utc.englishlearning.Encybara.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import utc.englishlearning.Encybara.domain.Admin;
import utc.englishlearning.Encybara.domain.Role;
import utc.englishlearning.Encybara.domain.response.ResultPaginationDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResAdminDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateAdmin;
import utc.englishlearning.Encybara.domain.response.auth.ResUpdateAdmin;
import utc.englishlearning.Encybara.repository.AdminRepository;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final RoleService roleService;

    public AdminService(AdminRepository adminRepository,
            RoleService roleService) {
        this.adminRepository = adminRepository;
        this.roleService = roleService;
    }

    public void updateAdminToken(String token, String email) {
        Admin adminUser = this.handleGetAdminByUsername(email);
        if (adminUser != null) {
            adminUser.setRefreshToken(token);
            this.adminRepository.save(adminUser);
        }
    }

    public Admin getAdminByRefreshTokenAndEmail(String token, String email) {
        return this.adminRepository.findByRefreshTokenAndEmail(token, email);
    }

    public Admin handleCreateAdmin(Admin admin) {

        // check role
        if (admin.getRole() != null) {
            Role r = this.roleService.fetchById(admin.getRole().getId());
            admin.setRole(r != null ? r : null);
        }

        return this.adminRepository.save(admin);
    }

    public void handleDeleteAdmin(long id) {
        this.adminRepository.deleteById(id);
    }

    public Admin fetchAdminById(long id) {
        Optional<Admin> adminOptional = this.adminRepository.findById(id);
        if (adminOptional.isPresent()) {
            return adminOptional.get();
        }
        return null;
    }

    public void invalidateTokens(String email) {
        Admin currentUser = this.handleGetAdminByUsername(email);
        if (currentUser != null) {
            // Xóa hoặc vô hiệu hóa token
            currentUser.setRefreshToken(null);
            this.adminRepository.save(currentUser);
        }
    }

    public ResultPaginationDTO fetchAllAdmin(Specification<Admin> spec, Pageable pageable) {
        Page<Admin> pageAdmin = this.adminRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageAdmin.getTotalPages());
        mt.setTotal(pageAdmin.getTotalElements());

        rs.setMeta(mt);

        // remove sensitive data
        List<ResAdminDTO> listAdmin = pageAdmin.getContent()
                .stream().map(item -> this.convertToResAdminDTO(item))
                .collect(Collectors.toList());

        rs.setResult(listAdmin);

        return rs;
    }

    public Admin handleUpdateAdmin(Admin reqAdmin) {
        Admin currentAdmin = this.fetchAdminById(reqAdmin.getId());
        if (currentAdmin != null) {
            currentAdmin.setEmail(reqAdmin.getEmail());
            currentAdmin.setName(reqAdmin.getName());
            currentAdmin.setField(reqAdmin.getField());
            currentAdmin.setPassword(reqAdmin.getPassword());

            // check company

            // check role
            if (reqAdmin.getRole() != null) {
                Role r = this.roleService.fetchById(reqAdmin.getRole().getId());
                currentAdmin.setRole(r != null ? r : null);
            }

            // update
            currentAdmin = this.adminRepository.save(currentAdmin);
        }
        return currentAdmin;
    }

    public Admin handleGetAdminByUsername(String username) {
        return this.adminRepository.findByEmail(username);
    }

    public boolean isEmailExist(String email) {
        return this.adminRepository.existsByEmail(email);
    }

    public ResAdminDTO convertToResAdminDTO(Admin admin) {
        ResAdminDTO res = new ResAdminDTO();
        ResAdminDTO.RoleUser roleAdmin = new ResAdminDTO.RoleUser();

        if (admin.getRole() != null) {
            roleAdmin.setId(admin.getRole().getId());
            roleAdmin.setName(admin.getRole().getName());
            res.setRole(roleAdmin);
        }

        res.setId(admin.getId());
        res.setEmail(admin.getEmail());
        res.setPassword(admin.getPassword());
        res.setName(admin.getName());
        res.setField(admin.getField());
        res.setUpdatedAt(admin.getUpdatedAt());
        res.setCreatedAt(admin.getCreatedAt());
        return res;
    }

    public ResCreateAdmin convertToResCreateAdminDTO(Admin admin) {
        ResCreateAdmin res = new ResCreateAdmin();

        res.setId(admin.getId());
        res.setEmail(admin.getEmail());
        res.setName(admin.getName());
        res.setField(admin.getField());
        res.setCreatedAt(admin.getCreatedAt());
        res.setPassword(admin.getPassword());
        return res;
    }

    public ResUpdateAdmin convertToResUpdateAdminDTO(Admin admin) {
        ResUpdateAdmin res = new ResUpdateAdmin();

        res.setId(admin.getId());
        res.setEmail(admin.getEmail());
        res.setName(admin.getName());
        res.setField(admin.getField());
        res.setCreatedAt(admin.getCreatedAt());
        res.setPassword(admin.getPassword());
        return res;
    }

}
