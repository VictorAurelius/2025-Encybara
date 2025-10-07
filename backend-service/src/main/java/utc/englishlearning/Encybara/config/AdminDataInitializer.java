package utc.englishlearning.Encybara.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.service.DataManagementService;
import utc.englishlearning.Encybara.service.CourseRecommendationRefreshService;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Service
public class AdminDataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataManagementService dataManagementService;
    private final LearningResultRepository learningResultRepository;
    private final CourseRecommendationRefreshService courseRecommendationRefreshService;

    public AdminDataInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            AdminRepository adminRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DataManagementService dataManagementService,
            LearningResultRepository learningResultRepository,
            CourseRecommendationRefreshService courseRecommendationRefreshService) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataManagementService = dataManagementService;
        this.learningResultRepository = learningResultRepository;
        this.courseRecommendationRefreshService = courseRecommendationRefreshService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT ADMIN CONFIG");
        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countAdmins = this.adminRepository.count();

        if (countPermissions == 0) {
            ArrayList<Permission> arr = new ArrayList<>();

            arr.add(new Permission("Get permissions with pagination", "/api/v1/permissions", "GET",
                    "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Update a permission", "/api/v1/permissions", "PUT", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "SYSTEM_MANAGEMENT"));

            // SYSTEM_MANAGEMENT ROLES
            arr.add(new Permission("Get roles with pagination", "/api/v1/roles", "GET", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Update a role", "/api/v1/roles", "PUT", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "SYSTEM_MANAGEMENT"));

            // SYSTEM_MANAGEMENT ADMINS
            arr.add(new Permission("Get users with pagination", "/api/v1/admins", "GET", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Create a user", "/api/v1/admins", "POST", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Update a user", "/api/v1/admins", "PUT", "SYSTEM_MANAGEMENT"));
            arr.add(new Permission("Delete a user", "/api/v1/admins/{id}", "DELETE", "SYSTEM_MANAGEMENT"));

            // CONTENT_MANAGEMENT COURSES
            arr.add(new Permission("Get courses with pagination", "/api/v1/courses", "GET", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Get a course by id", "/api/v1/courses/{id}", "GET", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Create a course", "/api/v1/courses", "POST", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Update a course", "/api/v1/courses", "PUT", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Delete a course", "/api/v1/courses/{id}", "DELETE", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Add a lesson to a course", "/api/v1/courses/{id}/lessons", "POST",
                    "CONTENT_MANAGEMENT"));

            // CONTENT_MANAGEMENT LESSONS
            arr.add(new Permission("Get lessons with pagination", "/api/v1/lessons", "GET", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Get a lesson by id", "/api/v1/lessons/{id}", "GET", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Create a lesson", "/api/v1/lessons", "POST", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Update a lesson", "/api/v1/lessons", "PUT", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Delete a lesson", "/api/v1/lessons/{id}", "DELETE", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Add a question to a lesson", "/api/v1/lessons/{id}/questions", "POST",
                    "CONTENT_MANAGEMENT"));

            // CONTENT_MANAGEMENT QUESTIONS
            arr.add(new Permission("Get questions with pagination", "/api/v1/questions", "GET", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Get a question by id", "/api/v1/questions/{id}", "GET", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Create a question", "/api/v1/questions", "POST", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Update a question", "/api/v1/questions", "PUT", "CONTENT_MANAGEMENT"));
            arr.add(new Permission("Delete a question", "/api/v1/questions/{id}", "DELETE", "CONTENT_MANAGEMENT"));

            this.permissionRepository.saveAll(arr);
        }

        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin thì full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);

            this.roleRepository.save(adminRole);
        }

        if (countAdmins == 0) {
            Admin adminUser = new Admin();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setField(null);
            adminUser.setName("I'm super admin");
            adminUser.setPassword(this.passwordEncoder.encode("123456"));

            Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            if (adminRole != null) {
                adminUser.setRole(adminRole);
            }

            this.adminRepository.save(adminUser);
        }

        // Create default user if none exists
        long countUsers = this.userRepository.count();
        if (countUsers == 0) {
            // Create default user
            utc.englishlearning.Encybara.domain.User defaultUser = new utc.englishlearning.Encybara.domain.User();
            defaultUser.setName("Encybara User");
            defaultUser.setEmail("user@example.com");
            defaultUser.setPassword(this.passwordEncoder.encode("Abc@123456"));
            defaultUser.setPhone("0123456789");
            defaultUser.setSpeciField(SpecialFieldEnum.IT);
            defaultUser.setRefreshToken("");

            // Create associated learning result
            Learning_Result learningResult = new Learning_Result();
            learningResult.setListeningScore(5.0);
            learningResult.setSpeakingScore(5.0);
            learningResult.setReadingScore(5.0);
            learningResult.setWritingScore(5.0);
            learningResult.setPreviousListeningScore(4.5);
            learningResult.setPreviousSpeakingScore(4.5);
            learningResult.setPreviousReadingScore(4.5);
            learningResult.setPreviousWritingScore(4.5);
            learningResult.setLastUpdated(Instant.now());
            learningResult.setUser(defaultUser);

            // Save user first
            defaultUser = userRepository.save(defaultUser);

            // Then save learning result
            learningResultRepository.save(learningResult);

            System.out.println(">>> DEFAULT USER CREATED");
        }

        if (countPermissions > 0 && countRoles > 0 && countAdmins > 0) {
            System.out.println(">>> SKIP INIT ADMIN CONFIG ~ ALREADY HAVE DATA...");
        } else {
            System.out.println(">>> END INIT ADMIN CONFIG");
        }

        // Seed course data
        System.out.println(">>> START SEEDING COURSE DATA");
        dataManagementService.seedPlacementData();
        dataManagementService.seedKet1Data();
        dataManagementService.seedKet3Data();
        dataManagementService.seedKet4Data();
        dataManagementService.seedKet5Data();
        dataManagementService.seedKet6Data();
        dataManagementService.seedKet7Data();
        dataManagementService.seedEFITData();
        System.out.println(">>> END SEEDING COURSE DATA");

        // Refresh course recommendations for all users
        System.out.println(">>> REFRESHING COURSE RECOMMENDATIONS");
        courseRecommendationRefreshService.refreshAllRecommendations();
    }
}
