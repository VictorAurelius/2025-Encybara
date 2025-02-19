package utc.englishlearning.Encybara.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
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
    private String baseURI;

    @Autowired
    private LearningMaterialRepository learningMaterialRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public void createDirectory(String folder) throws URISyntaxException {
        URI uri = new URI(folder);
        Path path = Paths.get(uri);
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectory(tmpDir.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }

    }

    public String getStoredFileName(String fullPath) {
        Path path = Paths.get(fullPath);
        return path.getFileName().toString();
    }

    public String store(MultipartFile file, String folder) throws IOException {
        // Tạo tên file mới
        String originalFilename = file.getOriginalFilename();
        String sanitizedFilename = sanitizeFileName(originalFilename);
        String finalName = System.currentTimeMillis() + "-" + sanitizedFilename;

        // Sử dụng Paths.get để tạo đường dẫn mà không có file://
        Path path = Paths.get(baseURI, folder, finalName);
        System.out.println("Saving file to: " + path.toString()); // In ra đường dẫn để kiểm tra

        // Kiểm tra và tạo thư mục nếu cần
        Files.createDirectories(path.getParent());

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    private String sanitizeFileName(String filename) {
        // Loại bỏ khoảng trắng và ký tự đặc biệt
        return filename.replaceAll("[^a-zA-Z0-9.\\-]", "_"); // Thay thế ký tự không hợp lệ bằng dấu gạch dưới
    }

    public long getFileLength(String fileName, String folder) throws URISyntaxException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File tmpDir = new File(path.toString());

        // file không tồn tại, hoặc file là 1 director => return 0
        if (!tmpDir.exists() || tmpDir.isDirectory())
            return 0;
        return tmpDir.length();
    }

    public InputStreamResource getResource(String fileName, String folder)
            throws URISyntaxException, FileNotFoundException {
        URI uri = new URI(baseURI + folder + "/" + fileName);
        Path path = Paths.get(uri);

        File file = new File(path.toString());
        return new InputStreamResource(new FileInputStream(file));
    }

    public String getFileNameById(long id) throws StorageException {
        Learning_Material learningMaterial = learningMaterialRepository.findById(id)
                .orElseThrow(() -> new StorageException("Tệp không tồn tại với ID = " + id));
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

}
