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
        courseDataSeeder.seedCourseData("ket1", "1", "1");
        // courseDataSeeder.seedCourseData("ket1", "1", "2");

        // Add more KET1 tests as needed
        // courseDataSeeder.seedCourseData("ket1", "2", "1");
        // courseDataSeeder.seedCourseData("ket1", "2", "2");
    }

    public void seedKet2Data() {
        // KET2 Test 1
        // courseDataSeeder.seedCourseData("ket2", "1", "1");
        // courseDataSeeder.seedCourseData("ket2", "1", "2");

        // Add more KET2 tests as needed
        // courseDataSeeder.seedCourseData("ket2", "2", "1");
        // courseDataSeeder.seedCourseData("ket2", "2", "2");
    }
}