package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.Question_Choice;
import utc.englishlearning.Encybara.domain.request.question.ReqCreateQuestionDTO;
import utc.englishlearning.Encybara.domain.request.question.ReqUpdateQuestionDTO;
import utc.englishlearning.Encybara.domain.response.question.ResQuestionDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.QuestionRepository;
import utc.englishlearning.Encybara.repository.QuestionChoiceRepository;
import utc.englishlearning.Encybara.specification.QuestionSpecification;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionChoiceRepository questionChoiceRepository;

    public ResQuestionDTO createQuestion(ReqCreateQuestionDTO questionDTO) {
        Question question = new Question();
        question.setQuesContent(questionDTO.getQuesContent());
        question.setKeyword(questionDTO.getKeyword());
        question.setQuesType(questionDTO.getQuesType());
        question.setPoint(questionDTO.getPoint());
        question.setSkillType(questionDTO.getSkillType());

        Question savedQuestion = questionRepository.save(question);
        List<Question_Choice> choices = questionDTO.getQuestionChoices();

        for (Question_Choice choice : choices) {
            choice.setQuestion(savedQuestion);
            questionChoiceRepository.save(choice);
        }

        return convertToDTO(savedQuestion);
    }

    public ResQuestionDTO updateQuestion(ReqUpdateQuestionDTO questionDTO) {
        Question question = questionRepository.findById(questionDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        question.setQuesContent(questionDTO.getQuesContent());
        question.setKeyword(questionDTO.getKeyword());
        question.setQuesType(questionDTO.getQuesType());
        question.setPoint(questionDTO.getPoint());
        question.setSkillType(questionDTO.getSkillType());

        questionRepository.save(question);
        return convertToDTO(question);
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found");
        }
        questionRepository.deleteById(id);
    }

    public ResQuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return convertToDTO(question);
    }

    public Page<ResQuestionDTO> getAllQuestions(Pageable pageable, String keyword, String content,
            QuestionTypeEnum quesType, Integer point) {
        Specification<Question> spec = Specification.where(QuestionSpecification.hasKeyword(keyword))
                .and(QuestionSpecification.hasQuesContent(content))
                .and(QuestionSpecification.hasQuesType(quesType))
                .and(QuestionSpecification.hasPoint(point));

        return questionRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    private ResQuestionDTO convertToDTO(Question question) {
        ResQuestionDTO dto = new ResQuestionDTO();
        dto.setId(question.getId());
        dto.setQuesContent(question.getQuesContent());
        dto.setKeyword(question.getKeyword());
        dto.setQuesType(question.getQuesType());
        dto.setSkillType(question.getSkillType());
        dto.setPoint(question.getPoint());
        dto.setQuestionChoices(question.getQuestionChoices());
        return dto;
    }
}