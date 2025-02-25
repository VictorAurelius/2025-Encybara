package utc.englishlearning.Encybara.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import utc.englishlearning.Encybara.exception.StorageException;
import utc.englishlearning.Encybara.service.LearningMaterialService;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.Learning_Material;
import utc.englishlearning.Encybara.repository.LearningMaterialRepository;
import utc.englishlearning.Encybara.service.QuestionService;
import utc.englishlearning.Encybara.domain.response.question.ResQuestionDTO;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.learningmaterial.ResUploadMaterialDTO;
import utc.englishlearning.Encybara.domain.request.learningmaterial.ReqAssignMaterialDTO;

@RestController
@RequestMapping("/api/v1/material")
public class LearningMaterialController {

    @Value("${englishlearning.upload-file.base-uri}")
    private String baseURI;

    private final LearningMaterialService fileService;
    private final QuestionService questionService;
    private final LearningMaterialRepository learning_MaterialRepository;

    public LearningMaterialController(LearningMaterialService fileService, QuestionService questionService,
            LearningMaterialRepository learning_MaterialRepository) {
        this.fileService = fileService;
        this.questionService = questionService;
        this.learning_MaterialRepository = learning_MaterialRepository;
    }

    @PostMapping("/upload/question")
    @ApiMessage("Upload material for a question")
    public ResponseEntity<RestResponse<ResUploadMaterialDTO>> uploadMaterialForQuestion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "materType", required = false) String materType)
            throws URISyntaxException, IOException, StorageException {

        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // Lưu file
        String fileName = fileService.store(file, folder);

        // Cập nhật tài liệu cho câu hỏi
        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(fileName);
        learningMaterial.setMaterType(materType != null ? materType : "application/octet-stream");
        learningMaterial.setUploadedAt(Instant.now()); // Set thời gian tải lên
        learningMaterial.setQuestionId(questionId); // Gán ID câu hỏi

        learning_MaterialRepository.save(learningMaterial);

        ResUploadMaterialDTO res = new ResUploadMaterialDTO(fileName, Instant.now(), learningMaterial.getMaterType(),
                questionId, null);
        RestResponse<ResUploadMaterialDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material uploaded for question successfully");
        response.setData(res);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/lesson")
    @ApiMessage("Upload material for a lesson")
    public ResponseEntity<RestResponse<ResUploadMaterialDTO>> uploadMaterialForLesson(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("lessonId") Long lessonId,
            @RequestParam(value = "materType", required = false) String materType)
            throws URISyntaxException, IOException, StorageException {

        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // Lưu file
        String fileName = fileService.store(file, folder);

        // Cập nhật tài liệu cho bài học
        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(fileName);
        learningMaterial.setMaterType(materType != null ? materType : "application/octet-stream");
        learningMaterial.setUploadedAt(Instant.now()); // Set thời gian tải lên
        learningMaterial.setLessonId(lessonId); // Gán ID bài học

        learning_MaterialRepository.save(learningMaterial);

        ResUploadMaterialDTO res = new ResUploadMaterialDTO(fileName, Instant.now(), learningMaterial.getMaterType(),
                null, lessonId);
        RestResponse<ResUploadMaterialDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material uploaded for lesson successfully");
        response.setData(res);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign/question")
    @ApiMessage("Assign existing material link to a question")
    public ResponseEntity<RestResponse<Void>> assignMaterialToQuestion(
            @RequestBody ReqAssignMaterialDTO reqAssignMaterialDTO)
            throws StorageException {
        // Cập nhật câu hỏi với link tài liệu
        ResQuestionDTO questionDTO = questionService.getQuestionById(reqAssignMaterialDTO.getQuestionId());
        if (questionDTO != null) {
            Learning_Material learningMaterial = new Learning_Material();
            Question question = convertToQuestion(questionDTO);

            learningMaterial.setQuestion(question);
            learningMaterial.setMaterLink(reqAssignMaterialDTO.getMaterLink());
            learningMaterial
                    .setMaterType(reqAssignMaterialDTO.getMaterType() != null ? reqAssignMaterialDTO.getMaterType()
                            : "application/octet-stream");
            learningMaterial.setUploadedAt(Instant.now()); // Cập nhật thời gian tải lên
            learning_MaterialRepository.save(learningMaterial);
        }

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material assigned to question successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign/lesson")
    @ApiMessage("Assign existing material link to a lesson")
    public ResponseEntity<RestResponse<Void>> assignMaterialToLesson(
            @RequestBody ReqAssignMaterialDTO reqAssignMaterialDTO)
            throws StorageException {
        // Cập nhật tài liệu cho bài học
        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(reqAssignMaterialDTO.getMaterLink());
        learningMaterial.setMaterType(reqAssignMaterialDTO.getMaterType() != null ? reqAssignMaterialDTO.getMaterType()
                : "application/octet-stream");
        learningMaterial.setLessonId(reqAssignMaterialDTO.getLessonId());
        learningMaterial.setUploadedAt(Instant.now()); // Cập nhật thời gian tải lên
        learning_MaterialRepository.save(learningMaterial);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material assigned to lesson successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/material/{id}")
    @ApiMessage("Get material link by ID")
    public ResponseEntity<RestResponse<String>> getMaterialLinkById(@PathVariable("id") Long id)
            throws StorageException {
        Learning_Material material = learning_MaterialRepository.findById(id)
                .orElseThrow(() -> new StorageException("Material not found with ID: " + id));

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material link retrieved successfully");
        response.setData(material.getMaterLink());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete material by ID")
    public ResponseEntity<RestResponse<Void>> deleteMaterialById(@PathVariable("id") Long id) throws StorageException {
        Learning_Material material = learning_MaterialRepository.findById(id)
                .orElseThrow(() -> new StorageException("Material not found with ID: " + id));

        learning_MaterialRepository.delete(material);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions/{questionId}")
    @ApiMessage("Get all learning materials link for a question")
    public ResponseEntity<RestResponse<List<Learning_Material>>> getLearningMaterialsByQuestionId(
            @PathVariable("questionId") Long questionId) {
        List<Learning_Material> materials = fileService.getLearningMaterialsByQuestionId(questionId);
        RestResponse<List<Learning_Material>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning materials retrieved successfully");
        response.setData(materials);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lessons/{lessonId}")
    @ApiMessage("Get all learning materials link for a lesson")
    public ResponseEntity<RestResponse<List<Learning_Material>>> getLearningMaterialsByLessonId(
            @PathVariable("lessonId") Long lessonId) {
        List<Learning_Material> materials = fileService.getLearningMaterialsByLessonId(lessonId);
        RestResponse<List<Learning_Material>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning materials retrieved successfully");
        response.setData(materials);
        return ResponseEntity.ok(response);
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
}