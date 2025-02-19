package utc.englishlearning.Encybara.domain.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utc.englishlearning.Encybara.domain.Role;

@Getter
@Setter
public class ResLoginAdminDTO {
    @JsonProperty("access_token")
    private String accessToken;

    private AdminLogin admin;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminLogin {
        private long id;
        private String email;
        private String name;
        private Role role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminGetAccount {
        private AdminLogin admin;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminInsideToken {
        private long id;
        private String email;
        private String name;
    }
}
