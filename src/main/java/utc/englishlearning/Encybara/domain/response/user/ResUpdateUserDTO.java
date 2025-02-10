package utc.englishlearning.Encybara.domain.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUpdateUserDTO {
    private long id;
    private String name;
    private String address;

    private String phone;
    private int field;
    private String avatar;
    private String englishlevel;
}
