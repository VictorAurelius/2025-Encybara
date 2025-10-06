package utc.englishlearning.Encybara.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${englishlearning.upload-file.base-uri}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose the upload directory
        Path uploadDir = Paths.get(uploadPath);
        String uploadAbsolutePath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploadfile/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/")
                .setCachePeriod(0);

        System.out.println("Configured resource handler: /uploadfile/** -> " + uploadAbsolutePath);
    }
}
