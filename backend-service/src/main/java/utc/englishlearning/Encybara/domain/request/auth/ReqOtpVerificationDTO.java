package utc.englishlearning.Encybara.domain.request.auth;

import utc.englishlearning.Encybara.domain.response.auth.ResCreateUserDTO;

public class ReqOtpVerificationDTO {
    private String otpID;
    private String email;
    private String otp;
    private ResCreateUserDTO userDTO;
    private long timestamp; // Thời gian tạo OTP
    private String type;

    // Constructor mặc định
    public ReqOtpVerificationDTO() {
    }

    // Constructor có tham số
    public ReqOtpVerificationDTO(String otpID, String otp, String email, ResCreateUserDTO userDTO, long timestamp,
            String type) {
        this.otpID = otpID;
        this.email = email;
        this.otp = otp;
        this.userDTO = userDTO;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getter và Setter cho otpID
    public String getOtpID() {
        return otpID;
    }

    public void setOtpID(String otpID) {
        this.otpID = otpID;
    }

    // Getter và Setter cho email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter và Setter cho otp
    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    // Getter và Setter cho userDTO
    public ResCreateUserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(ResCreateUserDTO userDTO) {
        this.userDTO = userDTO;
    }

    // Getter và Setter cho timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter và Setter cho type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
