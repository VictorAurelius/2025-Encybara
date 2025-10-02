package utc.englishlearning.Encybara.controller;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
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
import utc.englishlearning.Encybara.exception.LearningMaterialNotFoundException;
import utc.englishlearning.Encybara.service.FileStorageService;
import utc.englishlearning.Encybara.service.LearningMaterialService;
import utc.englishlearning.Encybara.util.annotation.ApiMessage;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.repository.CourseRepository;
import utc.englishlearning.Encybara.repository.LessonRepository;
import utc.englishlearning.Encybara.domain.Learning_Material;
import utc.englishlearning.Encybara.repository.LearningMaterialRepository;
import utc.englishlearning.Encybara.repository.QuestionRepository;
import utc.englishlearning.Encybara.domain.response.RestResponse;
import utc.englishlearning.Encybara.domain.response.learningmaterial.ResUploadMaterialDTO;
import utc.englishlearning.Encybara.domain.request.learningmaterial.ReqAssignMaterialDTO;

@RestController
@RequestMapping("/api/v1/material")
public class LearningMaterialController {

    private final LearningMaterialService fileService;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final LearningMaterialRepository learning_MaterialRepository;
    private final FileStorageService fileStorageService;
    private final CourseRepository courseRepository;

    public LearningMaterialController(
            LearningMaterialService fileService,
            QuestionRepository questionRepository,
            LessonRepository lessonRepository,
            LearningMaterialRepository learning_MaterialRepository,
            FileStorageService fileStorageService,
            CourseRepository courseRepository) {
        this.fileService = fileService;
        this.questionRepository = questionRepository;
        this.lessonRepository = lessonRepository;
        this.learning_MaterialRepository = learning_MaterialRepository;
        this.fileStorageService = fileStorageService;
        this.courseRepository = courseRepository;
    }

    @PostMapping("/upload/question")
    @ApiMessage("Upload material for a question")
    public ResponseEntity<RestResponse<ResUploadMaterialDTO>> uploadMaterialForQuestion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "materType", required = false) String materType)
            throws IOException, StorageException {

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // Get question to ensure it exists
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new LearningMaterialNotFoundException("Question not found"));

        // Save file and get full path
        String materLink = fileService.store(file, "questions/" + folder);

        // Create material record
        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(materLink);
        learningMaterial.setMaterType(materType != null ? materType : "application/octet-stream");
        learningMaterial.setUploadedAt(Instant.now());
        learningMaterial.setQuestion(question);

        learning_MaterialRepository.save(learningMaterial);

        ResUploadMaterialDTO res = new ResUploadMaterialDTO(materLink, Instant.now(), learningMaterial.getMaterType(),
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
            throws IOException, StorageException {

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // Get lesson to ensure it exists
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LearningMaterialNotFoundException("Lesson not found"));

        // Save file and get full path
        String materLink = fileService.store(file, "lessons/" + folder);

        // Create material record
        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(materLink);
        learningMaterial.setMaterType(materType != null ? materType : "application/octet-stream");
        learningMaterial.setUploadedAt(Instant.now());
        learningMaterial.setLesson(lesson);

        learning_MaterialRepository.save(learningMaterial);

        ResUploadMaterialDTO res = new ResUploadMaterialDTO(materLink, Instant.now(), learningMaterial.getMaterType(),
                null, lessonId);
        RestResponse<ResUploadMaterialDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material uploaded for lesson successfully");
        response.setData(res);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/course")
    @ApiMessage("Upload material for a course")
    public ResponseEntity<RestResponse<ResUploadMaterialDTO>> uploadMaterialForCourse(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "materType", required = false) String materType)
            throws IOException, StorageException {

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty. Please upload a file.");
        }

        // Get course to ensure it exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new LearningMaterialNotFoundException("Course not found"));

        // Save file and get full path
        String materLink = fileService.store(file, "courses/" + folder);

        // Create material record
        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(materLink);
        learningMaterial.setMaterType(materType != null ? materType : "application/octet-stream");
        learningMaterial.setUploadedAt(Instant.now());
        learningMaterial.setCourse(course);

        learning_MaterialRepository.save(learningMaterial);

        ResUploadMaterialDTO res = new ResUploadMaterialDTO(materLink, Instant.now(), learningMaterial.getMaterType(),
                null, null);
        res.setCourseId(courseId);
        RestResponse<ResUploadMaterialDTO> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material uploaded for course successfully");
        response.setData(res);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign/question")
    @ApiMessage("Assign existing material link to a question")
    public ResponseEntity<RestResponse<Void>> assignMaterialToQuestion(
            @RequestBody ReqAssignMaterialDTO reqAssignMaterialDTO)
            throws StorageException {
        Question question = questionRepository.findById(reqAssignMaterialDTO.getQuestionId())
                .orElseThrow(() -> new LearningMaterialNotFoundException("Question not found"));

        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setQuestion(question);
        learningMaterial.setMaterLink(reqAssignMaterialDTO.getMaterLink());
        learningMaterial
                .setMaterType(reqAssignMaterialDTO.getMaterType() != null ? reqAssignMaterialDTO.getMaterType()
                        : "application/octet-stream");
        learningMaterial.setUploadedAt(Instant.now());
        learning_MaterialRepository.save(learningMaterial);

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
        // Get lesson and verify it exists
        Lesson lesson = lessonRepository.findById(reqAssignMaterialDTO.getLessonId())
                .orElseThrow(() -> new LearningMaterialNotFoundException("Lesson not found"));

        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(reqAssignMaterialDTO.getMaterLink());
        learningMaterial.setMaterType(reqAssignMaterialDTO.getMaterType() != null ? reqAssignMaterialDTO.getMaterType()
                : "application/octet-stream");
        learningMaterial.setLesson(lesson);
        learningMaterial.setUploadedAt(Instant.now());
        learning_MaterialRepository.save(learningMaterial);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material assigned to lesson successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign/course")
    @ApiMessage("Assign existing material link to a course")
    public ResponseEntity<RestResponse<Void>> assignMaterialToCourse(
            @RequestBody ReqAssignMaterialDTO reqAssignMaterialDTO)
            throws StorageException {
        // Get course and verify it exists
        Course course = courseRepository.findById(reqAssignMaterialDTO.getCourseId())
                .orElseThrow(() -> new LearningMaterialNotFoundException("Course not found"));

        Learning_Material learningMaterial = new Learning_Material();
        learningMaterial.setMaterLink(reqAssignMaterialDTO.getMaterLink());
        learningMaterial.setMaterType(reqAssignMaterialDTO.getMaterType() != null ? reqAssignMaterialDTO.getMaterType()
                : "application/octet-stream");
        learningMaterial.setCourse(course);
        learningMaterial.setUploadedAt(Instant.now());
        learning_MaterialRepository.save(learningMaterial);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material assigned to course successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/material/{id}")
    @ApiMessage("Get material link by ID")
    public ResponseEntity<RestResponse<String>> getMaterialLinkById(@PathVariable("id") Long id) {
        Learning_Material material = learning_MaterialRepository.findById(id)
                .orElseThrow(() -> new LearningMaterialNotFoundException("Material not found"));

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material link retrieved successfully");
        response.setData(material.getMaterLink());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete material by ID")
    public ResponseEntity<RestResponse<Void>> deleteMaterialById(@PathVariable("id") Long id) {
        Learning_Material material = learning_MaterialRepository.findById(id)
                .orElseThrow(() -> new LearningMaterialNotFoundException("Material not found"));

        // Get the file path from material link and delete physical file
        try {
            // Use FileStorageService to delete the physical file
            fileStorageService.deleteFile(material.getMaterLink());
        } catch (Exception e) {
            // Log error but continue with database deletion
            System.err.println("Error deleting file: " + e.getMessage());
        }

        // Delete record from database
        learning_MaterialRepository.delete(material);

        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Material and associated file deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions/{questionId}")
    @ApiMessage("Get all learning materials link for a question")
    public ResponseEntity<RestResponse<List<Learning_Material>>> getLearningMaterialsByQuestionId(
            @PathVariable("questionId") Long questionId) {
        List<Learning_Material> materials = fileService.getLearningMaterialsByQuestionId(questionId);
        if (materials.isEmpty()) {
            throw new LearningMaterialNotFoundException("No materials found for question");
        }

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
        if (materials.isEmpty()) {
            throw new LearningMaterialNotFoundException("No materials found for lesson");
        }

        RestResponse<List<Learning_Material>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning materials retrieved successfully");
        response.setData(materials);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/{courseId}")
    @ApiMessage("Get all learning materials link for a course")
    public ResponseEntity<RestResponse<List<Learning_Material>>> getLearningMaterialsByCourseId(
            @PathVariable("courseId") Long courseId) {
        List<Learning_Material> materials = fileService.getLearningMaterialsByCourseId(courseId);
        if (materials.isEmpty()) {
            throw new LearningMaterialNotFoundException("No materials found for course");
        }

        RestResponse<List<Learning_Material>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Learning materials retrieved successfully");
        response.setData(materials);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses-with-materials")
    @ApiMessage("Get all course IDs that have learning materials")
    public ResponseEntity<RestResponse<List<Long>>> getCoursesWithLearningMaterials() {
        List<Long> courseIds = fileService.getCoursesWithLearningMaterials();

        RestResponse<List<Long>> response = new RestResponse<>();
        response.setStatusCode(200);
        response.setMessage("Course IDs with learning materials retrieved successfully");
        response.setData(courseIds);
        return ResponseEntity.ok(response);
    }

}