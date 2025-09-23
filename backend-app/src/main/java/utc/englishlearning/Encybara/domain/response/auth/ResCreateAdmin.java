package utc.englishlearning.Encybara.domain.response.auth;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Getter
@Setter
public class ResCreateAdmin {
    private long id;
    private String name;
    private String email;
    private String password;
    private SpecialFieldEnum field;
    private Instant createdAt;
}
