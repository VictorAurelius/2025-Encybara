package utc.englishlearning.Encybara.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import utc.englishlearning.Encybara.domain.RestResponse;
import utc.englishlearning.Encybara.domain.response.auth.ResRegisterDTO;
import utc.englishlearning.Encybara.domain.request.auth.ReqUpdatePasswordDTO;
import utc.englishlearning.Encybara.domain.request.*;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateUserDTO;

import utc.englishlearning.Encybara.service.EmailService;
import utc.englishlearning.Encybara.service.OtpService;
import utc.englishlearning.Encybara.service.UserService;
import utc.englishlearning.Encybara.util.SecurityUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/forgot-password")
public class ForgotPaswordController {

    private final UserService userService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;

    public ForgotPaswordController(UserService userService, EmailService emailService,
            OtpService otpService, SecurityUtil securityUtil,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.emailService = emailService;
        this.otpService = otpService;
        this.securityUtil = securityUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (!userService.isEmailExist(email)) {
            return ResponseEntity.badRequest().body("Invalid email");
        }
        String otp = otpService.generateOtp(email);
        ResCreateUserDTO temp = new ResCreateUserDTO();
        temp.setEmail(email);

        // off dong nay test api cho nhanh
        emailService.sendEmailFromTemplateSync(email, "Your OTP Code", otp);

        // System.out.println(otp);
        String otpID = otpService.saveRegisterData(email, temp, otp, "forgotpassword");
        RestResponse<ResRegisterDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("OTP sent to your email. Please verify to complete reset password.");
        response.setData(new ResRegisterDTO(otpID, "Expires in 2 minutes"));
        return ResponseEntity.ok(response);
    }

    // Gui lai otp khi het thoi gian se goi ben resend cua Auth vi id da map voi
    // mail

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(
            @RequestHeader("Authorization") String resetToken,
            @RequestBody ReqUpdatePasswordDTO updatePasswordRequest) {
        try {
            if (resetToken.startsWith("Bearer ")) {
                resetToken = resetToken.substring(7);
            }
            // giải mã token và lấy email
            Jwt token = securityUtil.checkValidResetPasswordToken(resetToken);
            String email = token.getSubject();

            // check điều mat khau
            if (!updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("Passwords do not match"));
            }
            String newPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
            if (!userService.updateUserPassword(email, newPassword)) {
                return ResponseEntity.badRequest().body(new ErrorResponseDTO("Update password failed"));
            }
            return ResponseEntity.ok("Password updated successfully");

        } catch (RuntimeException e) {
            // Xử lý lỗi khi token không hợp lệ hoặc hết hạn
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponseDTO(e.getMessage()));
        } catch (Exception e) {
            // Xử lý các lỗi khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDTO("An error occurred while updating password: " + e.getMessage()));
        }
    }

}
