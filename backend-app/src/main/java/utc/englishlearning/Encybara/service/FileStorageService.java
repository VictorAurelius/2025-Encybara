package utc.englishlearning.Encybara.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import utc.englishlearning.Encybara.exception.FileStorageException;

@Service
public class FileStorageService {

    @Value("${englishlearning.upload-file.base-uri}")
    private String baseUploadDir;

    @Value("${app.file.avatar-dir:avatars}")
    private String avatarDir;

    public String storeAvatar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty file");
            }

            // Create directories if they don't exist
            Path uploadPath = Paths.get(baseUploadDir, avatarDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename to prevent overwriting
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location
            Path targetLocation = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return a path suitable for serving via the configured static resource handler
            return avatarDir + "/" + newFilename;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!", ex);
        }
    }

    public void deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }

        try {
            Path path = Paths.get(baseUploadDir, filePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file at " + filePath + ". Please try again!", ex);
        }
    }
}
