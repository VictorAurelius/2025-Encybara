package utc.englishlearning.Encybara;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.main.allow-bean-definition-overriding=true"
})
class EncybaraApplicationTests {

	@Test
	void contextLoads() {
	}

}
