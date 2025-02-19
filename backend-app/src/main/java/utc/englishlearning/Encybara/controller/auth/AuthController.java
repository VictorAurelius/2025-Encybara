package utc.englishlearning.Encybara.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import utc.englishlearning.Encybara.domain.RestResponse;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.domain.response.auth.ResRegisterDTO;
import utc.englishlearning.Encybara.domain.request.auth.ReqLoginDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResLoginDTO;
import utc.englishlearning.Encybara.exception.IdInvalidException;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateUserDTO;
import utc.englishlearning.Encybara.service.EmailService;
import utc.englishlearning.Encybara.service.OtpService;
import utc.englishlearning.Encybara.service.UserService;
import utc.englishlearning.Encybara.util.SecurityUtil;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final SecurityUtil securityUtil;
        private final UserService userService;
        private final OtpService otpService;
        private final EmailService emailService;

        @Value("${englishlearning.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManager authenticationManager,
                        SecurityUtil securityUtil,
                        UserService userService,
                        OtpService otpService, EmailService emailService) {
                this.authenticationManager = authenticationManager;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.otpService = otpService;
                this.emailService = emailService;
        }

        @PostMapping("/login")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDto) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDto.getUsername(), loginDto.getPassword());
                Authentication authentication = authenticationManager.authenticate(authenticationToken);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO res = new ResLoginDTO();
                User currentUserDB = this.userService.handleGetUserByUsername(loginDto.getUsername());
                if (currentUserDB != null) {
                        res.setUser(new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName()));
                }

                String access_token = this.securityUtil.createAccessToken(authentication.getName(), res);
                res.setAccessToken(access_token);

                String refresh_token = this.securityUtil.createRefreshToken(loginDto.getUsername(), res);
                this.userService.updateUserToken(refresh_token, loginDto.getUsername());

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
        }

        @GetMapping("/account")
        public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
                String email = SecurityUtil.getCurrentUserLogin().orElse("");

                User currentUserDB = this.userService.handleGetUserByUsername(email);
                ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

                if (currentUserDB != null) {
                        userGetAccount.setUser(new ResLoginDTO.UserLogin(
                                        currentUserDB.getId(),
                                        currentUserDB.getEmail(),
                                        currentUserDB.getName()));
                }

                return ResponseEntity.ok().body(userGetAccount);
        }

        @PostMapping("/logout")
        public ResponseEntity<String> logout() throws IdInvalidException {
                String email = SecurityUtil.getCurrentUserLogin().orElse("");

                if (email.isEmpty()) {
                        throw new IdInvalidException("Access Token không hợp lệ");
                }

                // Vô hiệu hóa các token
                this.userService.invalidateTokens(email);

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

        @PostMapping("/register")
        public ResponseEntity<?> register(@Valid @RequestBody ResCreateUserDTO resCreateUserDto) {
                // Kiểm tra xem email đã tồn tại chưa
                if (userService.isEmailExist(resCreateUserDto.getEmail())) {
                        RestResponse<ResCreateUserDTO> errorResponse = new RestResponse<>();
                        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                        errorResponse.setMessage("Email already exists");
                        return ResponseEntity.badRequest().body(errorResponse);
                }

                // Tạo OTP
                String otp = otpService.generateOtp(resCreateUserDto.getEmail());

                // Gửi email OTP (bạn có thể bỏ dòng này khi test API)
                emailService.sendEmailFromTemplateSync(resCreateUserDto.getEmail(), "Your OTP Code", otp);

                // Lưu thông tin đăng ký
                String otpID = otpService.saveRegisterData(resCreateUserDto.getEmail(), resCreateUserDto, otp,
                                "register");
                // Tạo phản hồi

                RestResponse<ResRegisterDTO> response = new RestResponse<>();
                response.setStatusCode(HttpStatus.OK.value());
                response.setMessage("OTP sent to your email. Please verify to complete registration.");
                response.setData(new ResRegisterDTO(otpID, "Expires in 2 minutes"));

                return ResponseEntity.ok(response);
        }

}