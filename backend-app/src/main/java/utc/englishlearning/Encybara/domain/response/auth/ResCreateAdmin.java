package utc.englishlearning.Encybara.domain.response.auth;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCreateAdmin {
    private long id;
    private String name;
    private String email;
    private String password;
    private Integer field;
    private Instant createdAt;
}
