package utc.englishlearning.Encybara.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import utc.englishlearning.Encybara.domain.Admin;
import utc.englishlearning.Encybara.domain.response.ResultPaginationDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResAdminDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateAdmin;
import utc.englishlearning.Encybara.domain.response.auth.ResUpdateAdmin;
import utc.englishlearning.Encybara.exception.IdInvalidException;
import utc.englishlearning.Encybara.service.AdminService;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class AdminController {
    private final AdminService adminService;

    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminService adminService, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/admins")
    @ApiMessage("Create a new admin")
    public ResponseEntity<ResCreateAdmin> createNewUser(@Valid @RequestBody Admin postManUser)
            throws IdInvalidException {
        boolean isEmailExist = this.adminService.isEmailExist(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        Admin admin = this.adminService.handleCreateAdmin(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.adminService.convertToResCreateAdminDTO(admin));
    }

    @DeleteMapping("/admins/{id}")
    @ApiMessage("Delete a admin")
    public ResponseEntity<Void> deleteAdmin(@PathVariable("id") long id)
            throws IdInvalidException {
        Admin currentAdmin = this.adminService.fetchAdminById(id);
        if (currentAdmin == null) {
            throw new IdInvalidException("Admin với id = " + id + " không tồn tại");
        }

        this.adminService.handleDeleteAdmin(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/admins/{id}")
    @ApiMessage("fetch admin by id")
    public ResponseEntity<ResAdminDTO> getAdminById(@PathVariable("id") long id) throws IdInvalidException {
        Admin fetchAdmin = this.adminService.fetchAdminById(id);
        if (fetchAdmin == null) {
            throw new IdInvalidException("Admin với id = " + id + " không tồn tại");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.adminService.convertToResAdminDTO(fetchAdmin));
    }

    @GetMapping("/admins")
    @ApiMessage("fetch all admins")
    public ResponseEntity<ResultPaginationDTO> getAllAdmin(
            @Filter Specification<Admin> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(
                this.adminService.fetchAllAdmin(spec, pageable));
    }

    @PutMapping("/admins")
    @ApiMessage("Update a admin")
    public ResponseEntity<ResUpdateAdmin> updateUser(@RequestBody Admin admin) throws IdInvalidException {
        Admin ericUser = this.adminService.handleUpdateAdmin(admin);
        if (ericUser == null) {
            throw new IdInvalidException("Admin với id = " + admin.getId() + " không tồn tại");
        }
        return ResponseEntity.ok(this.adminService.convertToResUpdateAdminDTO(ericUser));
    }
}
