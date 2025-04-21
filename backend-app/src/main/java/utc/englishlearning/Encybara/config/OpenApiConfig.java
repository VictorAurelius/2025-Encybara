package utc.englishlearning.Encybara.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Encybara API Documentation", version = "1.0.0", description = "REST API documentation for Encybara English Learning System", contact = @Contact(name = "Encybara Team"), license = @License(name = "MIT License")), security = @SecurityRequirement(name = "Bearer Authentication"), servers = @Server(url = "/", description = "Default Server URL"))
@SecurityScheme(name = "Bearer Authentication", description = "JWT token authentication", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {
        // http://localhost:8080/swagger-ui.html
}