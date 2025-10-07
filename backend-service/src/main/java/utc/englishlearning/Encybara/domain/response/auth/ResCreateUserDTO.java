package utc.englishlearning.Encybara.domain.response.auth;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
public class ResCreateUserDTO {
    private long id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private SpecialFieldEnum speciField;
    private String avatar;
    private String englishlevel;

}
