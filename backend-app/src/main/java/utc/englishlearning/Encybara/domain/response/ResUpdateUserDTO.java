package utc.englishlearning.Encybara.domain.response;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
public class ResUpdateUserDTO {
    private long id;
    private String name;
    private String address;

    private String phone;
    private SpecialFieldEnum speciField;
    private String avatar;
    private String englishlevel;
}
