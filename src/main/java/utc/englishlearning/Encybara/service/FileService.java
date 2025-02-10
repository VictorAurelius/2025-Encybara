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
import utc.englishlearning.Encybara.exception.StorageException;
import utc.englishlearning.Encybara.repository.LearningMaterialRepository;

import java.util.List;

@Service
public class FileService {

    @Value("${englishlearning.upload-file.base-uri}")
    private String baseURI;

    @Autowired
    private LearningMaterialRepository learningMaterialRepository;

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

    public String store(MultipartFile file, String folder) throws URISyntaxException, IOException {
        // create unique filename
        String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

        URI uri = new URI(baseURI + folder + "/" + finalName);
        Path path = Paths.get(uri);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path,
                    StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
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
        return learningMaterial.getMaterLink(); // Giả sử đây là phương thức để lấy tên tệp
    }

    public String getFileNameByQuestionId(long id) throws StorageException {
        Learning_Material learningMaterial = learningMaterialRepository.findByQuestionId(id)
                .orElseThrow(() -> new StorageException("Tệp không tồn tại với ID = " + id));
        return learningMaterial.getMaterLink(); // Giả sử đây là phương thức để lấy tên tệp
    }

    // Phương thức để lấy tất cả MaterLink của một bài học
    public List<Learning_Material> getLearningMaterialsByLessonId(Long lessonId) {
        return learningMaterialRepository.findByLessonId(lessonId);
    }

}
