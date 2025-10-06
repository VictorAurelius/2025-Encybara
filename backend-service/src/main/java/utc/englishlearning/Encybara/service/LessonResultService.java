package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.request.lesson.ReqCreateLessonResultDTO;
import utc.englishlearning.Encybara.domain.response.lesson.ResLessonResultDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.annotation.PostConstruct;

@Service
public class LessonResultService {

        @PostConstruct
        @Transactional
        public void cleanupDuplicateLessonResults() {
                // Get all enrollments
                List<Enrollment> enrollments = enrollmentRepository.findAll();

                for (Enrollment enrollment : enrollments) {
                        // Get all lesson results for this enrollment
                        List<Lesson_Result> results = lessonResultRepository.findByEnrollment(enrollment);

                        // Group by lesson ID and keep only the highest score
                        Map<Long, Lesson_Result> bestResults = new HashMap<>();

                        for (Lesson_Result result : results) {
                                Long lessonId = result.getLesson().getId();
                                if (!bestResults.containsKey(lessonId) ||
                                                bestResults.get(lessonId).getTotalPoints() < result.getTotalPoints()) {
                                        bestResults.put(lessonId, result);
                                }
                        }

                        // Delete all results except the best ones
                        for (Lesson_Result result : results) {
                                if (!bestResults.containsValue(result)) {
                                        lessonResultRepository.delete(result);
                                }
                        }
                }
        }

        @Autowired
        private LessonResultRepository lessonResultRepository;

        @Autowired
        private AnswerRepository answerRepository;

        @Autowired
        private QuestionRepository questionRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private LessonRepository lessonRepository;

        @Autowired
        private EnrollmentRepository enrollmentRepository;

        private ResLessonResultDTO convertToDTO(Lesson_Result result) {
                ResLessonResultDTO dto = new ResLessonResultDTO();
                dto.setId(result.getId());
                dto.setUserId(result.getUser().getId());
                dto.setUserName(result.getUser().getName());
                dto.setLessonId(result.getLesson().getId());
                dto.setLessonName(result.getLesson().getName());
                dto.setStuTime(result.getStuTime());
                dto.setTotalPoints(result.getTotalPoints());
                dto.setComLevel(result.getComLevel());

                if (result.getEnrollment() != null) {
                        dto.setEnrollmentId(result.getEnrollment().getId());
                        if (result.getEnrollment().getCourse() != null) {
                                dto.setCourseType(result.getEnrollment().getCourse().getCourseType().toString());
                                dto.setDiffLevel(result.getEnrollment().getCourse().getDiffLevel());
                        }
                }

                if (result.getLesson() != null) {
                        dto.setSkillType(result.getLesson().getSkillType().toString());
                }

                return dto;
        }

        public Page<ResLessonResultDTO> getResultsByLessonIdAsDTO(Long lessonId, Pageable pageable) {
                return lessonResultRepository.findByLessonId(lessonId, pageable)
                                .map(this::convertToDTO);
        }

        public Page<ResLessonResultDTO> getResultsByUserIdAndLessonIdAsDTO(Long userId, Long lessonId,
                        Pageable pageable) {
                return lessonResultRepository.findByUserIdAndLessonId(userId, lessonId, pageable)
                                .map(this::convertToDTO);
        }

        public Page<ResLessonResultDTO> getLatestResultsByUserIdAsDTO(Long userId, Pageable pageable) {
                return lessonResultRepository.findByUserIdOrderBySessionIdDesc(userId, pageable)
                                .map(this::convertToDTO);
        }

        public List<ResLessonResultDTO> getLatestResultsByUserIdAndLessonIdAsDTO(Long userId, Long lessonId) {
                return lessonResultRepository.findByUserIdAndLessonIdOrderBySessionIdDesc(userId, lessonId)
                                .stream()
                                .map(this::convertToDTO)
                                .toList();
        }

        @Transactional
        public ResLessonResultDTO createLessonResultWithUserId(ReqCreateLessonResultDTO reqDto, Long userId) {
                Enrollment enrollment = enrollmentRepository.findById(reqDto.getEnrollmentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

                Lesson lesson = lessonRepository.findById(reqDto.getLessonId())
                                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

                // Check if result already exists
                Lesson_Result existingResult = lessonResultRepository.findByLessonIdAndEnrollmentId(
                                reqDto.getLessonId(), reqDto.getEnrollmentId());

                // Calculate points and completion level
                List<Question> questions = questionRepository.findByLesson(lesson);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                List<Answer> answers = answerRepository.findByUserAndQuestionInAndSessionId(
                                user, questions, reqDto.getSessionId());

                int totalPointsAchieved = answers.stream().mapToInt(Answer::getPoint_achieved).sum();
                int totalPointsPossible = questions.stream()
                                .mapToInt(Question::getPoint)
                                .sum();

                double comLevel = totalPointsPossible > 0
                                ? Math.min((double) totalPointsAchieved / totalPointsPossible * 100, 100.0)
                                : 0;

                Lesson_Result lessonResult;
                if (existingResult != null) {
                        // Update only if new score is better
                        if (totalPointsAchieved > existingResult.getTotalPoints()) {
                                lessonResult = existingResult;
                                lessonResult.setSessionId(reqDto.getSessionId());
                                lessonResult.setStuTime(reqDto.getStuTime());
                                lessonResult.setTotalPoints(totalPointsAchieved);
                                lessonResult.setComLevel(comLevel);
                                lessonResult = lessonResultRepository.save(lessonResult);
                        } else {
                                lessonResult = existingResult; // Keep existing result if new score isn't better
                        }
                } else {
                        // Create new result
                        lessonResult = new Lesson_Result();
                        lessonResult.setLesson(lesson);
                        lessonResult.setUser(user);
                        lessonResult.setEnrollment(enrollment);
                        lessonResult.setSessionId(reqDto.getSessionId());
                        lessonResult.setStuTime(reqDto.getStuTime());
                        lessonResult.setTotalPoints(totalPointsAchieved);
                        lessonResult.setComLevel(comLevel);
                        lessonResult = lessonResultRepository.save(lessonResult);
                }

                return convertToDTO(lessonResult);
        }

        // Keep these methods for backward compatibility but mark as deprecated
        @Deprecated
        public Page<Lesson_Result> getResultsByLessonId(Long lessonId, Pageable pageable) {
                return lessonResultRepository.findByLessonId(lessonId, pageable);
        }

        @Deprecated
        public Page<Lesson_Result> getResultsByUserIdAndLessonId(Long userId, Long lessonId, Pageable pageable) {
                return lessonResultRepository.findByUserIdAndLessonId(userId, lessonId, pageable);
        }

        @Deprecated
        public Page<Lesson_Result> getLatestResultsByUserId(Long userId, Pageable pageable) {
                return lessonResultRepository.findByUserIdOrderBySessionIdDesc(userId, pageable);
        }

        @Deprecated
        public List<Lesson_Result> getLatestResultsByUserIdAndLessonId(Long userId, Long lessonId) {
                return lessonResultRepository.findByUserIdAndLessonIdOrderBySessionIdDesc(userId, lessonId);
        }
}