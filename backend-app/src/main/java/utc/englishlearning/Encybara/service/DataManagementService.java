package utc.englishlearning.Encybara.service;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.data.seeding.CourseDataSeeder;

@Service
public class DataManagementService {
    private final CourseDataSeeder courseDataSeeder;

    public DataManagementService(CourseDataSeeder courseDataSeeder) {
        this.courseDataSeeder = courseDataSeeder;
    }

    public void seedKet1Data() {
        // KET1 Test 1
        courseDataSeeder.seedCourseData("ket1", "test1", "paper1");
        courseDataSeeder.seedCourseData("ket1", "test1", "paper2");
        courseDataSeeder.seedCourseData("ket1", "test1", "paper3");

        // Add more KET1 tests as needed
        // courseDataSeeder.seedCourseData("ket1", "2", "1");
        // courseDataSeeder.seedCourseData("ket1", "2", "2");
    }

    public void seedEFITData() {
        // EFIT Test 1
        courseDataSeeder.seedCourseData("efit", "unit1", "paper1");
        // courseDataSeeder.seedCourseData("efit", "test1", "paper2");
        // courseDataSeeder.seedCourseData("efit", "test1", "paper3");
    }
}