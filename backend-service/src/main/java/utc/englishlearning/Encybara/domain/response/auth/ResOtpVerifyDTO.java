package utc.englishlearning.Encybara.domain.response.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOtpVerifyDTO {
    private String message;
    private String token; // Chỉ sử dụng nếu type là "forgotPassword"

    public ResOtpVerifyDTO(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
