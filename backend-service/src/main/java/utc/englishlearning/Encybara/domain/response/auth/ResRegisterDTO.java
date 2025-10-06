package utc.englishlearning.Encybara.domain.response.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResRegisterDTO {
    private String otpID;
    private String timestamp;

    public ResRegisterDTO(String otpID, String timestamp) {
        this.timestamp = timestamp;
        this.otpID = otpID;
    }
}
