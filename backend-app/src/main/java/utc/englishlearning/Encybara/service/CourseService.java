package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.domain.Course_Lesson;
import utc.englishlearning.Encybara.domain.request.course.ReqAddLessonsToCourseDTO;
import utc.englishlearning.Encybara.domain.request.course.ReqCreateCourseDTO;
import utc.englishlearning.Encybara.domain.request.course.ReqUpdateCourseDTO;
import utc.englishlearning.Encybara.domain.response.course.ResCourseDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.exception.ResourceAlreadyExistsException;
import utc.englishlearning.Encybara.repository.CourseRepository;
import utc.englishlearning.Encybara.repository.LessonRepository;
import utc.englishlearning.Encybara.repository.CourseLessonRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseLessonRepository courseLessonRepository;

    public ResCourseDTO createCourse(ReqCreateCourseDTO reqCreateCourseDTO) {
        Course course = new Course();
        course.setName(reqCreateCourseDTO.getName());
        course.setIntro(reqCreateCourseDTO.getIntro());
        course.setDiffLevel(reqCreateCourseDTO.getDiffLevel());
        course.setRecomLevel(reqCreateCourseDTO.getRecomLevel());
        course.setCourseType(reqCreateCourseDTO.getCourseType());
        course.setSpeciField(reqCreateCourseDTO.getSpeciField());
        course = courseRepository.save(course);
        return convertToDTO(course);
    }

    public ResCourseDTO updateCourse(Long id, ReqUpdateCourseDTO reqUpdateCourseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setName(reqUpdateCourseDTO.getName());
        course.setIntro(reqUpdateCourseDTO.getIntro());
        course.setDiffLevel(reqUpdateCourseDTO.getDiffLevel());
        course.setRecomLevel(reqUpdateCourseDTO.getRecomLevel());
        course.setCourseType(reqUpdateCourseDTO.getCourseType());
        course.setSpeciField(reqUpdateCourseDTO.getSpeciField());
        course = courseRepository.save(course);
        return convertToDTO(course);
    }

    public ResCourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return convertToDTO(course);
    }

    public Page<ResCourseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    public void addLessonsToCourse(Long courseId, ReqAddLessonsToCourseDTO reqAddLessonsToCourseDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        List<Lesson> lessons = lessonRepository.findAllById(reqAddLessonsToCourseDTO.getLessonIds());
        if (lessons.size() != reqAddLessonsToCourseDTO.getLessonIds().size()) {
            throw new ResourceNotFoundException("One or more lessons not found");
        }

        List<Long> existingLessonIds = course.getCourselessons().stream()
                .map(cl -> cl.getLesson().getId())
                .collect(Collectors.toList());

        for (Lesson lesson : lessons) {
            if (existingLessonIds.contains(lesson.getId())) {
                throw new ResourceAlreadyExistsException(
                        "Lesson with ID " + lesson.getId() + " already exists in the course.");
            } else {
                Course_Lesson courseLesson = new Course_Lesson();
                courseLesson.setCourse(course);
                courseLesson.setLesson(lesson);
                course.getCourselessons().add(courseLesson);
                courseLessonRepository.save(courseLesson);
            }
        }

        // Cập nhật tổng số bài học
        course.setSumLesson(course.getCourselessons().size());
        courseRepository.save(course);
    }

    @Transactional
    public void removeLessonFromCourse(Long courseId, Long lessonId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean exists = course.getCourselessons().stream()
                .anyMatch(cl -> cl.getLesson().getId() == lessonId);

        if (!exists) {
            throw new ResourceNotFoundException("Lesson with ID " + lessonId + " does not exist in the course.");
        }

        course.getCourselessons().removeIf(cl -> cl.getLesson().getId() == lessonId);
        courseLessonRepository.deleteByLesson_IdAndCourse_Id(lessonId, courseId);

        // Cập nhật tổng số bài học
        course.setSumLesson(course.getCourselessons().size());
        courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        courseRepository.delete(course);
    }

    private ResCourseDTO convertToDTO(Course course) {
        ResCourseDTO dto = new ResCourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setIntro(course.getIntro());
        dto.setDiffLevel(course.getDiffLevel());
        dto.setRecomLevel(course.getRecomLevel());
        dto.setCourseType(course.getCourseType());
        dto.setSpeciField(course.getSpeciField());
        dto.setCreateBy(course.getCreateBy());
        dto.setCreateAt(course.getCreateAt());
        dto.setUpdateBy(course.getUpdateBy());
        dto.setUpdateAt(course.getUpdateAt());
        dto.setSumLesson(course.getSumLesson());

        List<Long> lessonIds = (course.getCourselessons() != null) ? course.getCourselessons().stream()
                .map(cl -> cl.getLesson().getId())
                .collect(Collectors.toList()) : new ArrayList<>();

        dto.setLessonIds(lessonIds.isEmpty() ? null : lessonIds);
        return dto;
    }
}