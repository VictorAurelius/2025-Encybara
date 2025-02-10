package utc.englishlearning.Encybara.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import utc.englishlearning.Encybara.domain.response.file.ResUploadFileDTO;
import utc.englishlearning.Encybara.exception.StorageException;
import utc.englishlearning.Encybara.service.FileService;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.Learning_Material;
import utc.englishlearning.Encybara.repository.LearningMaterialRepository;
import utc.englishlearning.Encybara.service.QuestionService;
import utc.englishlearning.Encybara.domain.response.question.ResQuestionDTO;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    @Value("${englishlearning.upload-file.base-uri}")
    private String baseURI;

    private final FileService fileService;
    private final QuestionService questionService;
    private final LearningMaterialRepository learning_MaterialRepository;

    public FileController(FileService fileService, QuestionService questionService,
            LearningMaterialRepository learning_MaterialRepository) {
        this.fileService = fileService;
        this.questionService = questionService;
        this.learning_MaterialRepository = learning_MaterialRepository;
    }

    @PostMapping("/files")
    @ApiMessage("Upload single file")
    public ResponseEntity<ResUploadFileDTO> upload(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam("folder") String folder) throws URISyntaxException, IOException, StorageException {

        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new StorageException("File name is null. Please upload a valid file.");
        }

        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx", "mp3", "txt");
        boolean isValid = allowedExtensions.stream().anyMatch(item -> fileName.toLowerCase().endsWith(item));

        if (!isValid) {
            throw new StorageException("Invalid file extension. only allows " + allowedExtensions.toString());
        }

        // Tạo thư mục nếu chưa tồn tại
        this.fileService.createDirectory(baseURI + folder);

        // Lưu file
        String uploadFile = this.fileService.store(file, folder);

        ResUploadFileDTO res = new ResUploadFileDTO(uploadFile, Instant.now());
        return ResponseEntity.ok().body(res);
    }

    @GetMapping("/files")
    @ApiMessage("Download a file")
    public ResponseEntity<Resource> download(
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestParam(name = "folder", required = false) String folder)
            throws StorageException, URISyntaxException, FileNotFoundException {
        if (fileName == null || folder == null) {
            throw new StorageException("Missing required params : (fileName or folder) in query params.");
        }

        // check file exist (and not a directory)
        long fileLength = this.fileService.getFileLength(fileName, folder);
        if (fileLength == 0) {
            throw new StorageException("File with name = " + fileName + " not found.");
        }

        // download a file
        InputStreamResource resource = this.fileService.getResource(fileName, folder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(fileLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/files/listening")
    @ApiMessage("Upload mp3 file for listening question")
    public ResponseEntity<ResUploadFileDTO> uploadListeningFile(
            @RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "questionId") Long questionId)
            throws URISyntaxException, IOException, StorageException {
        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // Lưu file mp3
        String fileName = fileService.store(file, "listening"); // Giả sử bạn có thư mục "listening"

        // Cập nhật câu hỏi với link mp3
        ResQuestionDTO questionDTO = questionService.getQuestionById(questionId);
        if (questionDTO != null) {
            Learning_Material learningMaterial = new Learning_Material();

            // Convert ResQuestionDTO to Question
            Question question = convertToQuestion(questionDTO);

            learningMaterial.setQuestion(question); // Now this works
            learningMaterial.setMaterLink(fileName);
            learningMaterial.setMaterType("audio/mpeg");
            learning_MaterialRepository.save(learningMaterial);
        }

        ResUploadFileDTO res = new ResUploadFileDTO(fileName, Instant.now());
        return ResponseEntity.ok().body(res);
    }

    // Method to convert ResQuestionDTO to Question
    private Question convertToQuestion(ResQuestionDTO questionDTO) {
        Question question = new Question();
        question.setId(questionDTO.getId());
        question.setQuesContent(questionDTO.getQuesContent());
        question.setKeyword(questionDTO.getKeyword());
        question.setQuesType(questionDTO.getQuesType());
        question.setSkillType(questionDTO.getSkillType());
        question.setPoint(questionDTO.getPoint());
        // Set other properties if needed
        return question;
    }

    @GetMapping("/files/names/{id}")
    @ApiMessage("Get file name by ID")
    public ResponseEntity<String> getFileNameById(@PathVariable("id") long id)
            throws StorageException {

        // Lấy tên tệp dựa trên ID
        String fileName = this.fileService.getFileNameByQuestionId(id);

        return ResponseEntity.ok(fileName);
    }

    @GetMapping("/lessons/{lessonId}/materials")
    @ApiMessage("Get all learning materials for a lesson")
    public ResponseEntity<List<Learning_Material>> getLearningMaterialsByLessonId(@PathVariable Long lessonId) {
        List<Learning_Material> materials = fileService.getLearningMaterialsByLessonId(lessonId);
        return ResponseEntity.ok(materials);
    }
}