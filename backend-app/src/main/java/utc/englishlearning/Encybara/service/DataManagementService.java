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

    }

    public void seedKet3Data() {
        courseDataSeeder.seedCourseData("ket3", "test1", "paper1");
        courseDataSeeder.seedCourseData("ket3", "test1", "paper2");
        courseDataSeeder.seedCourseData("ket3", "test1", "paper3");
        courseDataSeeder.seedCourseData("ket3", "test1", "paper4");
    }
    public void seedKet4Data() {
        courseDataSeeder.seedCourseData("ket4", "test1", "paper1");
        courseDataSeeder.seedCourseData("ket4", "test1", "paper2");
        courseDataSeeder.seedCourseData("ket4", "test1", "paper3");
        courseDataSeeder.seedCourseData("ket4", "test1", "paper4");
    }
    public void seedKet5Data() {
        courseDataSeeder.seedCourseData("ket5", "test1", "paper1");
        courseDataSeeder.seedCourseData("ket5", "test1", "paper2");
        courseDataSeeder.seedCourseData("ket5", "test1", "paper3");
        courseDataSeeder.seedCourseData("ket5", "test1", "paper4");
    }
    public void seedKet6Data() {
        courseDataSeeder.seedCourseData("ket6", "test1", "paper1");
        courseDataSeeder.seedCourseData("ket6", "test1", "paper2");
        courseDataSeeder.seedCourseData("ket6", "test1", "paper3");
        courseDataSeeder.seedCourseData("ket6", "test1", "paper4");
    }
    public void seedKet7Data() {
        courseDataSeeder.seedCourseData("ket7", "test1", "paper1");
        courseDataSeeder.seedCourseData("ket7", "test1", "paper2");
        courseDataSeeder.seedCourseData("ket7", "test1", "paper3");
        courseDataSeeder.seedCourseData("ket7", "test1", "paper4");
        courseDataSeeder.seedCourseData("ket7", "test2", "paper1");
        courseDataSeeder.seedCourseData("ket7", "test2", "paper2");
        courseDataSeeder.seedCourseData("ket7", "test2", "paper4");
        courseDataSeeder.seedCourseData("ket7", "test3", "paper2");
        courseDataSeeder.seedCourseData("ket7", "test3", "paper4");
    }
    public void seedEFITData() {
        // EFIT Test 1
        courseDataSeeder.seedCourseData("efit", "unit1", "paper1");
        courseDataSeeder.seedCourseData("efit", "unit1", "paper2");
        courseDataSeeder.seedCourseData("efit", "unit1", "paper3");
        courseDataSeeder.seedCourseData("efit", "unit1", "paper4");
        courseDataSeeder.seedCourseData("efit", "unit1", "paper5");
        courseDataSeeder.seedCourseData("efit", "unit2", "paper1");
        courseDataSeeder.seedCourseData("efit", "unit2", "paper2");
        courseDataSeeder.seedCourseData("efit", "unit2", "paper3");
        courseDataSeeder.seedCourseData("efit", "unit2", "paper4");
        courseDataSeeder.seedCourseData("efit", "unit2", "paper5");
    }

    public void seedPlacementData() {
        // EFIT Test 1
        courseDataSeeder.seedCourseData("placement", "test1", "paper1");
    }
}