package utc.englishlearning.Encybara.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {
    private long id;
    private String email;
    private String name;

    private String phone;
    private SpecialFieldEnum speciField;
    private String avatar;
    private String englishlevel;
}
