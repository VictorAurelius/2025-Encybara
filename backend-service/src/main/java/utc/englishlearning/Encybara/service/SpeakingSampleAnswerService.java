package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.SpeakingSampleAnswer;
import utc.englishlearning.Encybara.domain.request.speaking.ReqCreateSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.domain.request.speaking.ReqUpdateSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.domain.response.speaking.ResSpeakingSampleAnswerDTO;
import utc.englishlearning.Encybara.exception.InvalidOperationException;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.QuestionRepository;
import utc.englishlearning.Encybara.repository.SpeakingSampleAnswerRepository;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpeakingSampleAnswerService {

    @Autowired
    private SpeakingSampleAnswerRepository speakingSampleAnswerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * Create a new speaking sample answer
     */
    public ResSpeakingSampleAnswerDTO createSpeakingSampleAnswer(ReqCreateSpeakingSampleAnswerDTO requestDTO) {
        // Validate question exists and is a speaking question
        Question question = questionRepository.findById(requestDTO.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + requestDTO.getQuestionId()));

        if (question.getQuesType() != QuestionTypeEnum.SPEAKING) {
            throw new InvalidOperationException("Sample answers can only be added to SPEAKING questions");
        }

        // Create new sample answer
        SpeakingSampleAnswer sampleAnswer = new SpeakingSampleAnswer();
        sampleAnswer.setAnswerContent(requestDTO.getAnswerContent());
        sampleAnswer.setDescription(requestDTO.getDescription());
        sampleAnswer.setDifficultyLevel(requestDTO.getDifficultyLevel());
        sampleAnswer.setEstimatedScore(requestDTO.getEstimatedScore());
        sampleAnswer.setQuestion(question);

        SpeakingSampleAnswer savedAnswer = speakingSampleAnswerRepository.save(sampleAnswer);
        return convertToResponseDTO(savedAnswer);
    }

    /**
     * Update an existing speaking sample answer
     */
    public ResSpeakingSampleAnswerDTO updateSpeakingSampleAnswer(ReqUpdateSpeakingSampleAnswerDTO requestDTO) {
        SpeakingSampleAnswer existingAnswer = speakingSampleAnswerRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Speaking sample answer not found with id: " + requestDTO.getId()));

        // Update fields
        existingAnswer.setAnswerContent(requestDTO.getAnswerContent());
        existingAnswer.setDescription(requestDTO.getDescription());
        existingAnswer.setDifficultyLevel(requestDTO.getDifficultyLevel());
        existingAnswer.setEstimatedScore(requestDTO.getEstimatedScore());

        SpeakingSampleAnswer savedAnswer = speakingSampleAnswerRepository.save(existingAnswer);
        return convertToResponseDTO(savedAnswer);
    }

    /**
     * Get speaking sample answer by ID
     */
    public ResSpeakingSampleAnswerDTO getSpeakingSampleAnswerById(Long id) {
        SpeakingSampleAnswer sampleAnswer = speakingSampleAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaking sample answer not found with id: " + id));
        return convertToResponseDTO(sampleAnswer);
    }

    /**
     * Get all sample answers for a specific question
     */
    public List<ResSpeakingSampleAnswerDTO> getSampleAnswersByQuestionId(Long questionId) {
        // Validate question exists and is a speaking question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (question.getQuesType() != QuestionTypeEnum.SPEAKING) {
            throw new InvalidOperationException("Sample answers can only be retrieved for SPEAKING questions");
        }

        List<SpeakingSampleAnswer> sampleAnswers = speakingSampleAnswerRepository.findByQuestionId(questionId);
        return sampleAnswers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get sample answers by question ID and difficulty level
     */
    public List<ResSpeakingSampleAnswerDTO> getSampleAnswersByQuestionIdAndDifficulty(Long questionId,
            Integer difficultyLevel) {
        // Validate question exists and is a speaking question
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (question.getQuesType() != QuestionTypeEnum.SPEAKING) {
            throw new InvalidOperationException("Sample answers can only be retrieved for SPEAKING questions");
        }

        List<SpeakingSampleAnswer> sampleAnswers = speakingSampleAnswerRepository
                .findByQuestionIdAndDifficultyLevel(questionId, difficultyLevel);
        return sampleAnswers.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a speaking sample answer
     */
    public void deleteSpeakingSampleAnswer(Long id) {
        if (!speakingSampleAnswerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Speaking sample answer not found with id: " + id);
        }
        speakingSampleAnswerRepository.deleteById(id);
    }

    /**
     * Check if a question has sample answers
     */
    public boolean questionHasSampleAnswers(Long questionId) {
        return speakingSampleAnswerRepository.existsByQuestionId(questionId);
    }

    /**
     * Get count of sample answers for a question
     */
    public Long countSampleAnswersByQuestionId(Long questionId) {
        return speakingSampleAnswerRepository.countByQuestionId(questionId);
    }

    /**
     * Convert entity to response DTO
     */
    private ResSpeakingSampleAnswerDTO convertToResponseDTO(SpeakingSampleAnswer sampleAnswer) {
        ResSpeakingSampleAnswerDTO responseDTO = new ResSpeakingSampleAnswerDTO();
        responseDTO.setId(sampleAnswer.getId());
        responseDTO.setAnswerContent(sampleAnswer.getAnswerContent());
        responseDTO.setDescription(sampleAnswer.getDescription());
        responseDTO.setDifficultyLevel(sampleAnswer.getDifficultyLevel());
        responseDTO.setEstimatedScore(sampleAnswer.getEstimatedScore());
        responseDTO.setQuestionId(sampleAnswer.getQuestion().getId());
        responseDTO.setQuestionContent(sampleAnswer.getQuestion().getQuesContent());
        responseDTO.setCreateBy(sampleAnswer.getCreateBy());
        responseDTO.setCreateAt(sampleAnswer.getCreateAt());
        responseDTO.setUpdateBy(sampleAnswer.getUpdateBy());
        responseDTO.setUpdateAt(sampleAnswer.getUpdateAt());
        return responseDTO;
    }
}