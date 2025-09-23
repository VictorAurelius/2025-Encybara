package utc.englishlearning.Encybara.domain.request.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdatePasswordDTO {
//    String resetToken;
    String newPassword;
    String confirmPassword;


}
