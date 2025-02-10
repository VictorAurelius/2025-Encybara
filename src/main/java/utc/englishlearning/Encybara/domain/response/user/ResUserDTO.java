package utc.englishlearning.Encybara.domain.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {
    private long id;
    private String email;
    private String name;

    private String phone;
    private int field;
    private String avatar;
    private String englishlevel;
}
