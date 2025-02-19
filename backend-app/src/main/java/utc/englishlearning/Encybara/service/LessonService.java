package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.Lesson;
import utc.englishlearning.Encybara.domain.Lesson_Question;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.request.lesson.ReqAddQuestionsToLessonDTO;
import utc.englishlearning.Encybara.domain.request.lesson.ReqCreateLessonDTO;
import utc.englishlearning.Encybara.domain.request.lesson.ReqUpdateLessonDTO;
import utc.englishlearning.Encybara.domain.response.lesson.ResLessonDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.LessonRepository;
import utc.englishlearning.Encybara.repository.QuestionRepository;
import utc.englishlearning.Encybara.repository.LessonQuestionRepository;
import utc.englishlearning.Encybara.exception.ResourceAlreadyExistsException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private LessonQuestionRepository lessonQuestionRepository;

    public ResLessonDTO createLesson(ReqCreateLessonDTO reqCreateLessonDTO) {
        Lesson lesson = new Lesson();
        lesson.setName(reqCreateLessonDTO.getName());
        lesson.setSkillType(reqCreateLessonDTO.getSkillType());
        lesson = lessonRepository.save(lesson);
        return convertToDTO(lesson);
    }

    public ResLessonDTO updateLesson(Long id, ReqUpdateLessonDTO reqUpdateLessonDTO) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        lesson.setName(reqUpdateLessonDTO.getName());
        lesson.setSkillType(reqUpdateLessonDTO.getSkillType());
        lesson = lessonRepository.save(lesson);
        return convertToDTO(lesson);
    }

    public ResLessonDTO getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        return convertToDTO(lesson);
    }

    public Page<ResLessonDTO> getAllLessons(Pageable pageable) {
        return lessonRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    public void addQuestionsToLesson(Long lessonId, ReqAddQuestionsToLessonDTO reqAddQuestionsToLessonDTO) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        List<Question> questions = questionRepository.findAllById(reqAddQuestionsToLessonDTO.getQuestionIds());
        if (questions.size() != reqAddQuestionsToLessonDTO.getQuestionIds().size()) {
            throw new ResourceNotFoundException("One or more questions not found");
        }

        List<Long> existingQuestionIds = lesson.getLessonquestions().stream()
                .map(lq -> lq.getQuestion().getId())
                .collect(Collectors.toList());

        List<Long> newQuestionIds = new ArrayList<>();
        for (Question question : questions) {
            if (existingQuestionIds.contains(question.getId())) {
                throw new ResourceAlreadyExistsException(
                        "Question with ID " + question.getId() + " already exists in the lesson.");
            } else {
                newQuestionIds.add(question.getId());
                Lesson_Question lessonQuestion = new Lesson_Question();
                lessonQuestion.setLesson(lesson);
                lessonQuestion.setQuestion(question);
                lessonQuestionRepository.save(lessonQuestion);
            }
        }

        lesson.setSumQues(lesson.getLessonquestions().size() + newQuestionIds.size());
        lessonRepository.save(lesson);
    }

    @Transactional
    public void removeQuestionFromLesson(Long lessonId, Long questionId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        boolean exists = lesson.getLessonquestions().stream()
                .anyMatch(lq -> lq.getQuestion().getId() == questionId);

        if (!exists) {
            throw new ResourceNotFoundException("Question with ID " + questionId + " does not exist in the lesson.");
        }

        lessonQuestionRepository.deleteByLessonIdAndQuestionId(lessonId, questionId);
        lesson.setSumQues(lesson.getLessonquestions().size());
        lessonRepository.save(lesson);
    }

    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lesson not found");
        }
        lessonRepository.deleteById(id);
    }

    private ResLessonDTO convertToDTO(Lesson lesson) {
        ResLessonDTO dto = new ResLessonDTO();
        dto.setId(lesson.getId());
        dto.setName(lesson.getName());
        dto.setSkillType(lesson.getSkillType());
        dto.setCreateBy(lesson.getCreateBy());
        dto.setCreateAt(lesson.getCreateAt());
        dto.setUpdateBy(lesson.getUpdateBy());
        dto.setUpdateAt(lesson.getUpdateAt());

        List<Long> questionIds = (lesson.getLessonquestions() != null) ? lesson.getLessonquestions().stream()
                .map(lq -> lq.getQuestion().getId())
                .collect(Collectors.toList()) : new ArrayList<>();

        dto.setQuestionIds(questionIds.isEmpty() ? null : questionIds);

        return dto;
    }
}