package utc.englishlearning.Encybara.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import utc.englishlearning.Encybara.domain.response.RestResponse;
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
    public ResponseEntity<RestResponse<ResRegisterDTO>> requestResetPassword(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        RestResponse<ResRegisterDTO> response = new RestResponse<>();

        if (!userService.isEmailExist(email)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid email");
            return ResponseEntity.badRequest().body(response);
        }

        String otp = otpService.generateOtp(email);
        ResCreateUserDTO temp = new ResCreateUserDTO();
        temp.setEmail(email);

        emailService.sendEmailFromTemplateSync(email, "Your OTP Code", otp);
        String otpID = otpService.saveRegisterData(email, temp, otp, "forgotpassword");

        response.setStatusCode(200);
        response.setMessage("OTP sent successfully");
        response.setData(new ResRegisterDTO(otpID, "Expires in 2 minutes"));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-password")
    public ResponseEntity<RestResponse<String>> updatePassword(
            @RequestHeader("Authorization") String resetToken,
            @RequestBody ReqUpdatePasswordDTO updatePasswordRequest) {

        RestResponse<String> response = new RestResponse<>();

        try {
            if (resetToken.startsWith("Bearer ")) {
                resetToken = resetToken.substring(7);
            }

            Jwt token = securityUtil.checkValidResetPasswordToken(resetToken);
            String email = token.getSubject();

            if (!updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getConfirmPassword())) {
                response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                response.setMessage("Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }

            String newPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
            if (!userService.updateUserPassword(email, newPassword)) {
                response.setStatusCode(HttpStatus.BAD_REQUEST.value());
                response.setMessage("Update password failed");
                return ResponseEntity.badRequest().body(response);
            }

            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Password updated successfully");
            response.setData("Password has been updated");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("An error occurred while updating password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
