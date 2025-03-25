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
import utc.englishlearning.Encybara.util.constant.SkillTypeEnum;

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

        // Validate that question has appropriate skillType based on quesType
        if (question.getQuesType() == QuestionTypeEnum.WRITING || question.getQuesType() == QuestionTypeEnum.SPEAKING) {
            // For WRITING questions, skillType should also be WRITING
            if (question.getQuesType() == QuestionTypeEnum.WRITING
                    && question.getSkillType() != SkillTypeEnum.WRITING) {
                throw new IllegalArgumentException("WRITING questions must have WRITING skill type");
            }

            // For SPEAKING questions, skillType should also be SPEAKING
            if (question.getQuesType() == QuestionTypeEnum.SPEAKING
                    && question.getSkillType() != SkillTypeEnum.SPEAKING) {
                throw new IllegalArgumentException("SPEAKING questions must have SPEAKING skill type");
            }

            // Ensure the keyword is properly set for WRITING/SPEAKING questions
            if (question.getKeyword() == null || question.getKeyword().trim().isEmpty()) {
                if (question.getQuesType() == QuestionTypeEnum.WRITING) {
                    question.setKeyword("writing task");
                } else {
                    question.setKeyword("speaking task");
                }
            }
        }

        // Ensure required fields are not null
        if (question.getQuesContent() == null || question.getQuesContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Question content cannot be empty");
        }

        // Fix the issue with null comparison - proper way to check Integer object
        if (question.getPoint() <= 0) {
            throw new IllegalArgumentException("Question point must be positive");
        }

        // Check if WRITING/SPEAKING question type has choices (not allowed)
        List<Question_Choice> choices = questionDTO.getQuestionChoices();
        if ((question.getQuesType() == QuestionTypeEnum.WRITING || question.getQuesType() == QuestionTypeEnum.SPEAKING)
                && choices != null && !choices.isEmpty()) {
            throw new IllegalArgumentException(question.getQuesType() + " questions cannot have choices");
        }

        Question savedQuestion = questionRepository.save(question);

        // Only add choices if they exist and question is not WRITING/SPEAKING type
        if (choices != null && !choices.isEmpty()) {
            for (Question_Choice choice : choices) {
                choice.setQuestion(savedQuestion);
                questionChoiceRepository.save(choice);
            }
        }

        return convertToDTO(savedQuestion);
    }

    public ResQuestionDTO updateQuestion(ReqUpdateQuestionDTO questionDTO) {
        Question question = questionRepository.findById(questionDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // Update the question fields
        question.setQuesContent(questionDTO.getQuesContent());
        question.setKeyword(questionDTO.getKeyword());
        question.setQuesType(questionDTO.getQuesType());
        question.setPoint(questionDTO.getPoint());
        question.setSkillType(questionDTO.getSkillType());

        // Validate that question has appropriate skillType based on quesType
        if (question.getQuesType() == QuestionTypeEnum.WRITING || question.getQuesType() == QuestionTypeEnum.SPEAKING) {
            // For WRITING questions, skillType should also be WRITING
            if (question.getQuesType() == QuestionTypeEnum.WRITING
                    && question.getSkillType() != SkillTypeEnum.WRITING) {
                throw new IllegalArgumentException("WRITING questions must have WRITING skill type");
            }

            // For SPEAKING questions, skillType should also be SPEAKING
            if (question.getQuesType() == QuestionTypeEnum.SPEAKING
                    && question.getSkillType() != SkillTypeEnum.SPEAKING) {
                throw new IllegalArgumentException("SPEAKING questions must have SPEAKING skill type");
            }

            // Ensure the keyword is properly set for WRITING/SPEAKING questions
            if (question.getKeyword() == null || question.getKeyword().trim().isEmpty()) {
                if (question.getQuesType() == QuestionTypeEnum.WRITING) {
                    question.setKeyword("writing task");
                } else {
                    question.setKeyword("speaking task");
                }
            }
        }

        // Ensure required fields are not null
        if (question.getQuesContent() == null || question.getQuesContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Question content cannot be empty");
        }

        // Fix the issue with null comparison - proper way to check Integer object
        if (question.getPoint() <= 0) {
            throw new IllegalArgumentException("Question point must be positive");
        }

        // Check if WRITING/SPEAKING question type has choices (not allowed)
        if ((question.getQuesType() == QuestionTypeEnum.WRITING || question.getQuesType() == QuestionTypeEnum.SPEAKING)
                &&
                questionDTO.getQuestionChoices() != null &&
                !questionDTO.getQuestionChoices().isEmpty()) {
            throw new IllegalArgumentException(question.getQuesType() + " questions cannot have choices");
        }

        // Save the updated question
        questionRepository.save(question);

        // Only process choices if the question is not WRITING/SPEAKING type
        if (question.getQuesType() != QuestionTypeEnum.WRITING && question.getQuesType() != QuestionTypeEnum.SPEAKING) {
            // Update question choices
            List<Question_Choice> existingChoices = questionChoiceRepository.findByQuestionId(question.getId());

            // Update existing choices and add new ones
            for (Question_Choice choice : questionDTO.getQuestionChoices()) {
                if (choice.getId() > 0) {
                    // Update existing choice
                    Question_Choice existingChoice = existingChoices.stream()
                            .filter(c -> c.getId() == choice.getId())
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Choice not found"));
                    existingChoice.setChoiceContent(choice.getChoiceContent());
                    questionChoiceRepository.save(existingChoice);
                } else {
                    // Create new choice
                    choice.setQuestion(question);
                    questionChoiceRepository.save(choice);
                }
            }

            // Delete choices that are no longer associated
            for (Question_Choice existingChoice : existingChoices) {
                if (questionDTO.getQuestionChoices().stream()
                        .noneMatch(c -> c.getId() > 0 && c.getId() == existingChoice.getId())) {
                    questionChoiceRepository.delete(existingChoice);
                }
            }
        } else {
            // For WRITING/SPEAKING questions, remove any existing choices
            List<Question_Choice> existingChoices = questionChoiceRepository.findByQuestionId(question.getId());
            for (Question_Choice choice : existingChoices) {
                questionChoiceRepository.delete(choice);
            }
        }

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
            QuestionTypeEnum quesType, Integer point, SkillTypeEnum skillType) {
        Specification<Question> spec = Specification.where(QuestionSpecification.hasKeyword(keyword))
                .and(QuestionSpecification.hasQuesContent(content))
                .and(QuestionSpecification.hasQuesType(quesType))
                .and(QuestionSpecification.hasPoint(point))
                .and(QuestionSpecification.hasSkillType(skillType));

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