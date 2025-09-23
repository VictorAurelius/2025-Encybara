package utc.englishlearning.Encybara.controller.admin;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import utc.englishlearning.Encybara.domain.Admin;
import utc.englishlearning.Encybara.domain.request.auth.ReqLoginDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateAdmin;
import utc.englishlearning.Encybara.domain.response.auth.ResLoginAdminDTO;
import utc.englishlearning.Encybara.service.AdminService;
import utc.englishlearning.Encybara.util.SecurityUtil;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;
import utc.englishlearning.Encybara.exception.IdInvalidException;
import org.springframework.security.authentication.AuthenticationManager;

@RestController
@RequestMapping("/api/v1")
public class AuthAdminController {

    private final AuthenticationManager adminAuthManager;
    private final SecurityUtil securityUtil;
    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;

    @Value("${englishlearning.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthAdminController(@Qualifier("adminAuthManager") AuthenticationManager adminAuthManager,
            SecurityUtil securityUtil,
            AdminService adminService,
            PasswordEncoder passwordEncoder) {
        this.adminAuthManager = adminAuthManager;
        this.securityUtil = securityUtil;
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ResLoginAdminDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) {
        // Nạp input gồm username/password vào Security
        System.out.println("Attempting to log in with username: " + loginDto.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());

        try {
            // xác thực người dùng => cần viết hàm loadUserByUsername
            Authentication authentication = adminAuthManager.authenticate(authenticationToken);

            // set thông tin người dùng đăng nhập vào context (có thể sử dụng sau này)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            ResLoginAdminDTO res = new ResLoginAdminDTO();
            Admin currentUserDB = this.adminService.handleGetAdminByUsername(loginDto.getUsername());
            System.out.println("ở đây " + currentUserDB);
            if (currentUserDB != null) {
                ResLoginAdminDTO.AdminLogin adminLogin = new ResLoginAdminDTO.AdminLogin(
                        currentUserDB.getId(),
                        currentUserDB.getEmail(),
                        currentUserDB.getName(),
                        currentUserDB.getRole());
                res.setAdmin(adminLogin);
            }

            // create access token
            String access_token = this.securityUtil.createAdminAccessToken(authentication.getName(), res);
            res.setAccessToken(access_token);

            // create refresh token
            String refresh_token = this.securityUtil.createAdminRefreshToken(loginDto.getUsername(), res);

            // update user
            this.adminService.updateAdminToken(refresh_token, loginDto.getUsername());

            // set cookies
            ResponseCookie resCookies = ResponseCookie
                    .from("refresh_token", refresh_token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                    .body(res);
        } catch (Exception e) {
            // Log the authentication error
            System.out.println("Authentication failed for username: " + loginDto.getUsername());
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/admin/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginAdminDTO.AdminGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        Admin currentAdminDB = this.adminService.handleGetAdminByUsername(email);
        ResLoginAdminDTO.AdminLogin adminLogin = new ResLoginAdminDTO.AdminLogin();
        ResLoginAdminDTO.AdminGetAccount adminGetAccount = new ResLoginAdminDTO.AdminGetAccount();

        if (currentAdminDB != null) {
            adminLogin.setId(currentAdminDB.getId());
            adminLogin.setEmail(currentAdminDB.getEmail());
            adminLogin.setName(currentAdminDB.getName());
            adminLogin.setRole(currentAdminDB.getRole());

            adminGetAccount.setAdmin(adminLogin);
        }

        return ResponseEntity.ok().body(adminGetAccount);
    }

    @GetMapping("/admin/refresh")
    @ApiMessage("Get User by refresh token")
    public ResponseEntity<ResLoginAdminDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token) throws IdInvalidException {
        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Bạn không có refresh token ở cookie");
        }
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();

        // check user by token + email
        Admin currentAdmin = this.adminService.getAdminByRefreshTokenAndEmail(refresh_token, email);
        if (currentAdmin == null) {
            throw new IdInvalidException("Refresh Token không hợp lệ");
        }

        // issue new token/set refresh token as cookies
        ResLoginAdminDTO res = new ResLoginAdminDTO();
        Admin currentAdminDB = this.adminService.handleGetAdminByUsername(email);
        if (currentAdminDB != null) {
            ResLoginAdminDTO.AdminLogin adminLogin = new ResLoginAdminDTO.AdminLogin(
                    currentAdminDB.getId(),
                    currentAdminDB.getEmail(),
                    currentAdminDB.getName(),
                    currentAdminDB.getRole());
            res.setAdmin(adminLogin);
        }

        // create access token
        String access_token = this.securityUtil.createAdminAccessToken(email, res);
        res.setAccessToken(access_token);

        // create refresh token
        String new_refresh_token = this.securityUtil.createAdminRefreshToken(email, res);

        // update user
        this.adminService.updateAdminToken(new_refresh_token, email);

        // set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @PostMapping("/admin/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<String> logout() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access Token không hợp lệ");
        }

        // Vô hiệu hóa các token
        this.adminService.invalidateTokens(email);

        // Xóa cookie refresh_token
        ResponseCookie deleteRefreshTokenCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        // Xóa cookie access_token nếu có
        ResponseCookie deleteAccessTokenCookie = ResponseCookie
                .from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteAccessTokenCookie.toString())
                .body("Đăng xuất thành công");
    }

    @PostMapping("/admin/register")
    @ApiMessage("Register a new user")
    public ResponseEntity<ResCreateAdmin> register(@Valid @RequestBody Admin postManAdmin) throws IdInvalidException {
        boolean isEmailExist = this.adminService.isEmailExist(postManAdmin.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManAdmin.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(postManAdmin.getPassword());
        postManAdmin.setPassword(hashPassword);
        Admin ericUser = this.adminService.handleCreateAdmin(postManAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.adminService.convertToResCreateAdminDTO(ericUser));
    }
}
