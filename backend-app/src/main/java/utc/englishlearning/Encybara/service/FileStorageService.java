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

    @Value("${server.address}")
    private String serverIp;

    @Value("${server.port}")
    private String serverPort;

    /**
     * Stores an avatar file and returns a direct URL to access it
     */
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

            // Return a direct URL to access the file
            String materialLink = generateMaterialLink(avatarDir + "/" + newFilename);
            return materialLink;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!", ex);
        }
    }

    /**
     * Generates a direct URL for accessing a file
     */
    public String generateMaterialLink(String relativePath) {
        // Format the URL as http://server-ip:port/uploadfile/relative-path
        return String.format("http://%s:%s/uploadfile/%s", serverIp, serverPort, relativePath);
    }

    /**
     * Extracts the relative path from a material link
     */
    public String extractPathFromMaterialLink(String materialLink) {
        if (materialLink == null || materialLink.isEmpty()) {
            return null;
        }

        String prefix = String.format("http://%s:%s/uploadfile/", serverIp, serverPort);
        if (materialLink.startsWith(prefix)) {
            return materialLink.substring(prefix.length());
        }

        // If not a material link, return as is (might be a relative path already)
        return materialLink;
    }

    public void deleteFile(String materialLink) {
        if (materialLink == null || materialLink.trim().isEmpty()) {
            return;
        }

        try {
            // Extract the path from the material link
            String relativePath = extractPathFromMaterialLink(materialLink);
            if (relativePath != null) {
                Path path = Paths.get(baseUploadDir, relativePath);
                Files.deleteIfExists(path);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file at " + materialLink + ". Please try again!", ex);
        }
    }
}
