package utc.englishlearning.Encybara.domain.request.auth;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateUserDTO;

@Getter
@Setter
public class ReqOtpVerificationDTO {
    private  String otpID;
    private String email;
    private String otp;
    private ResCreateUserDTO userDTO;
    private long timestamp; // Thời gian tạo OTP
    private String type;

    public ReqOtpVerificationDTO(String otpID, String otp, String email, ResCreateUserDTO userDTO, long timestamp, String type) {
        this.otpID = otpID;
        this.email = email;
        this.otp = otp;
        this.userDTO = userDTO;
        this.timestamp = timestamp;
        this.type = type;
    }
}
