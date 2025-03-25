package utc.englishlearning.Encybara.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import utc.englishlearning.Encybara.domain.Learning_Material;
import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.exception.StorageException;
import utc.englishlearning.Encybara.repository.LearningMaterialRepository;
import utc.englishlearning.Encybara.repository.LessonRepository;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.repository.QuestionRepository;

import java.util.List;

@Service
public class LearningMaterialService {

    @Value("${englishlearning.upload-file.base-uri}")
    private String baseUri;

    private final LearningMaterialRepository learningMaterialRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    public LearningMaterialService(
            @Value("${englishlearning.upload-file.base-uri}") String baseURI,
            LearningMaterialRepository learningMaterialRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository) {
        this.baseUri = baseURI;
        this.learningMaterialRepository = learningMaterialRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
    }

    private void validateFolderPath(String folder) throws StorageException {
        if (folder == null || folder.contains("..")) {
            throw new StorageException("Invalid folder path: " + folder);
        }
    }

    private void createDirectory(String folder) throws IOException, StorageException {
        validateFolderPath(folder);
        Path basePath = getUploadPath();
        Path folderPath = basePath.resolve(folder).normalize();

        // Security check - ensure the folder is within base path
        if (!folderPath.startsWith(basePath)) {
            throw new StorageException("Cannot create directory outside of upload path");
        }

        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }

    public String getStoredFileName(String fullPath) {
        Path path = Paths.get(fullPath);
        return path.getFileName().toString();
    }

    private Path getUploadPath() {
        return Paths.get(baseUri).toAbsolutePath().normalize();
    }

    public String buildResourcePath(String relativePath) {
        // Clean and normalize the path
        String normalizedPath = relativePath.replace("\\", "/");

        // Remove any leading slashes since baseURI includes them
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }

        return normalizedPath;
    }

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private String serverPort;

    public String getServerUrl() {
        return String.format("http://%s:%s", serverAddress, serverPort);
    }

    private String buildMaterialLink(String folder, String filename) {
        // Ensure the folder path is normalized and uses correct separators
        String normalizedFolder = folder.replace("\\", "/");
        if (!normalizedFolder.startsWith("/")) {
            normalizedFolder = "/" + normalizedFolder;
        }
        if (!normalizedFolder.endsWith("/")) {
            normalizedFolder = normalizedFolder + "/";
        }

        // Build URL with server IP and port
        return getServerUrl() + "/uploadfile" + normalizedFolder + filename;
    }

    public String store(MultipartFile file, String folder) throws IOException {
        // Normalize base path
        Path basePath = getUploadPath();

        // Create folder structure
        Path folderPath = basePath.resolve(folder);
        Files.createDirectories(folderPath);

        // Create unique filename
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFileName(originalFilename);
        String finalName = System.currentTimeMillis() + "-" + sanitizedFilename;

        // Get full file path for storage
        Path filePath = folderPath.resolve(finalName);

        // Store the file
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Return the full material link for database storage
        return buildMaterialLink(folder, finalName);
    }

    private String sanitizeFileName(String filename) {
        // Remove whitespace and special characters
        return filename.replaceAll("[^a-zA-Z0-9.\\-]", "_"); // Replace invalid characters with underscore
    }

    private Path validateAndGetFilePath(String materLink) throws FileNotFoundException {
        if (materLink == null) {
            throw new FileNotFoundException("Material link is null");
        }
        try {
            // Extract the relative path from the URL
            String serverUrl = getServerUrl() + "/uploadfile";
            String relativePath = materLink;

            if (materLink.startsWith(serverUrl)) {
                relativePath = materLink.substring(serverUrl.length());
            }

            // Ensure the relativePath starts with a slash
            if (!relativePath.startsWith("/")) {
                relativePath = "/" + relativePath;
            }

            // Log the paths for debugging
            System.out.println("Material Link: " + materLink);
            System.out.println("Server URL: " + serverUrl);
            System.out.println("Relative Path: " + relativePath);
            System.out.println("Base URI: " + baseUri);

            // Ensure baseUri ends with slash
            String normalizedBaseUri = baseUri.replace("\\", "/");
            if (!normalizedBaseUri.endsWith("/")) {
                normalizedBaseUri = normalizedBaseUri + "/";
            }

            // Construct the actual file path with proper slash handling
            String fullPath = normalizedBaseUri + relativePath.substring(1).replace("\\", "/");
            System.out.println("Full Path: " + fullPath);
            Path filePath = Paths.get(fullPath);

            // Check file existence and readability
            if (Files.exists(filePath)) {
                System.out.println("File exists at: " + filePath);
                if (!Files.isReadable(filePath)) {
                    System.out.println("File is not readable: " + filePath);
                    throw new FileNotFoundException("File is not readable: " + relativePath);
                }
            } else {
                System.out.println("File does not exist at: " + filePath);
                throw new FileNotFoundException("File not found: " + relativePath);
            }

            if (!Files.isRegularFile(filePath)) {
                System.out.println("Path is not a regular file: " + filePath);
                throw new FileNotFoundException("Not a regular file: " + relativePath);
            }

            return filePath;
        } catch (Exception e) {
            System.out.println("Error accessing file: " + e.getMessage());
            e.printStackTrace();
            throw new FileNotFoundException("Error accessing file: " + e.getMessage());
        }
    }

    public long getFileLength(String materLink) throws IOException {
        Path filePath = validateAndGetFilePath(materLink);
        return Files.size(filePath);
    }

    public InputStreamResource getResource(String materLink) throws IOException {
        try {
            Path filePath = validateAndGetFilePath(materLink);
            return new InputStreamResource(Files.newInputStream(filePath));
        } catch (Exception e) {
            System.out.println("Error getting resource: " + materLink + ", Error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Could not read file: " + materLink, e);
        }
    }

    public String getFileNameById(long id) throws StorageException {
        Learning_Material learningMaterial = learningMaterialRepository.findById(id)
                .orElseThrow(() -> new StorageException("File not found with ID: " + id));
        return learningMaterial.getMaterLink();
    }

    public List<Learning_Material> getLearningMaterialsByLessonId(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));
        return learningMaterialRepository.findByLesson(lesson);
    }

    public List<Learning_Material> getLearningMaterialsByQuestionId(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));
        return learningMaterialRepository.findByQuestion(question);
    }

    public Path getFilePath(String relativePath) {
        return Paths.get(baseUri, relativePath);
    }

    public boolean fileExists(String relativePath) {
        Path filePath = getFilePath(relativePath);
        return filePath.toFile().exists();
    }

}
