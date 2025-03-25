package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.domain.response.answer.ResAnswerDTO;
import utc.englishlearning.Encybara.domain.request.answer.ReqCreateAnswerDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.*;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnswerService {

        @Autowired
        private AnswerRepository answerRepository;

        @Autowired
        private QuestionRepository questionRepository;

        @Autowired
        private AnswerTextRepository answerTextRepository;

        @Autowired
        private QuestionChoiceRepository questionChoiceRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private EnrollmentRepository enrollmentRepository;

        @Transactional
        public ResAnswerDTO createAnswerWithUserId(ReqCreateAnswerDTO reqDto, Long userId) {
                // Get required entities
                Question question = questionRepository.findById(reqDto.getQuestionId())
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Get enrollment if provided
                Enrollment enrollment = null;
                if (reqDto.getEnrollmentId() != null) {
                        enrollment = enrollmentRepository.findById(reqDto.getEnrollmentId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

                        // Validate that the enrollment belongs to the user
                        if (!Objects.equals(enrollment.getUser().getId(), userId)) {
                                throw new IllegalArgumentException("Enrollment does not belong to the user");
                        }
                }

                // Get previous answers to calculate session ID
                List<Answer> previousAnswers = answerRepository.findByUserAndQuestion(user, question);
                long newSessionId = previousAnswers.size() + 1;

                // Create and save answer
                Answer answer = new Answer();
                answer.setQuestion(question);
                answer.setUser(user);
                answer.setEnrollment(enrollment);
                answer.setPoint_achieved(reqDto.getPointAchieved() != null ? reqDto.getPointAchieved() : 0);
                answer.setImprovement(reqDto.getImprovement());
                answer.setSessionId(newSessionId);
                answer = answerRepository.save(answer);

                // Create and save answer text with proper bidirectional relationship
                Answer_Text answerText = new Answer_Text();
                answerText.setAnsContent(reqDto.getAnswerContent());
                answerText.setAnswer(answer);
                answerText = answerTextRepository.save(answerText);

                // Update answer with the text reference
                answer.setAnswerText(answerText);
                answer = answerRepository.save(answer);

                return convertToDTO(answer, answer.getAnswerText());
        }

        public ResAnswerDTO getAnswerById(Long id) {
                Answer answer = answerRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
                Answer_Text answerText = answerTextRepository.findByAnswer(answer)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer text not found"));
                return convertToDTO(answer, answerText);
        }

        public Page<Answer> getAnswersByQuestionId(Long questionId, Pageable pageable) {
                List<Answer> allAnswers = answerRepository.findAll().stream()
                                .filter(answer -> Objects.equals(answer.getQuestion().getId(), questionId))
                                .collect(Collectors.toList());

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allAnswers.size());
                return new PageImpl<>(allAnswers.subList(start, end), pageable, allAnswers.size());
        }

        public Page<Answer> getAllAnswersByQuestionIdAndUserId(Long questionId, Long userId, Pageable pageable) {
                List<Answer> allAnswers = answerRepository.findAll().stream()
                                .filter(answer -> Objects.equals(answer.getQuestion().getId(), questionId)
                                                && Objects.equals(answer.getUser().getId(), userId))
                                .collect(Collectors.toList());

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allAnswers.size());
                return new PageImpl<>(allAnswers.subList(start, end), pageable, allAnswers.size());
        }

        @Transactional
        public ResAnswerDTO gradeAnswer(Long answerId) {
                Answer answer = answerRepository.findById(answerId)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

                Question question = answer.getQuestion();
                Answer_Text answerText = answerTextRepository.findByAnswer(answer)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer text not found for grading"));

                String userAnswer = answerText.getAnsContent().trim();
                List<Question_Choice> choices = questionChoiceRepository.findByQuestionId(question.getId());

                if (question.getQuesType() == QuestionTypeEnum.MULTIPLE) {
                        // Handle multiple choice questions
                        List<String> correctChoices = choices.stream()
                                        .filter(Question_Choice::isChoiceKey)
                                        .map(choice -> choice.getChoiceContent().trim())
                                        .collect(Collectors.toList());

                        List<String> userChoices = List.of(userAnswer.split("\\s*,\\s*"));

                        boolean isFullyCorrect = correctChoices.size() == userChoices.size()
                                        && correctChoices.stream()
                                                        .allMatch(correct -> userChoices.stream()
                                                                        .anyMatch(user -> normalizeAnswer(user)
                                                                                        .equals(normalizeAnswer(
                                                                                                        correct))));

                        if (isFullyCorrect) {
                                answer.setPoint_achieved(question.getPoint());
                        } else {
                                long correctCount = userChoices.stream()
                                                .filter(userChoice -> correctChoices.stream()
                                                                .anyMatch(correct -> normalizeAnswer(userChoice)
                                                                                .equals(normalizeAnswer(correct))))
                                                .count();

                                double partialPoint = (double) correctCount / correctChoices.size()
                                                * question.getPoint();
                                answer.setPoint_achieved((int) Math.round(partialPoint));
                        }
                } else if (question.getQuesType() == QuestionTypeEnum.CHOICE) {
                        // Handle single choice questions
                        boolean isCorrect = choices.stream()
                                        .filter(Question_Choice::isChoiceKey)
                                        .anyMatch(choice -> normalizeAnswer(choice.getChoiceContent())
                                                        .equals(normalizeAnswer(userAnswer)));

                        answer.setPoint_achieved(isCorrect ? question.getPoint() : 0);
                } else if (question.getQuesType() == QuestionTypeEnum.TEXT) {
                        // Handle text questions - compare with the correct answer choice
                        boolean isCorrect = choices.stream()
                                        .filter(Question_Choice::isChoiceKey)
                                        .anyMatch(choice -> normalizeAnswer(choice.getChoiceContent())
                                                        .equals(normalizeAnswer(userAnswer)));

                        answer.setPoint_achieved(isCorrect ? question.getPoint() : 0);
                }

                answer = answerRepository.save(answer);
                return convertToDTO(answer, answerText);
        }

        public ResAnswerDTO getLatestAnswerByUserAndQuestion(Long questionId, Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Question question = questionRepository.findById(questionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

                List<Answer> userAnswers = answerRepository.findByUserAndQuestion(user, question);

                return userAnswers.stream()
                                .max((a1, a2) -> Long.compare(a1.getSessionId(), a2.getSessionId()))
                                .map(answer -> {
                                        Answer_Text answerText = answerTextRepository.findByAnswer(answer)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Answer text not found"));
                                        return convertToDTO(answer, answerText);
                                })
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No answers found for this user and question"));
        }

        private ResAnswerDTO convertToDTO(Answer answer, Answer_Text answerText) {
                ResAnswerDTO dto = new ResAnswerDTO();
                dto.setId(answer.getId());
                dto.setQuestionId(answer.getQuestion().getId());
                dto.setAnswerContent(answerText.getAnsContent());
                dto.setPointAchieved(answer.getPoint_achieved());
                dto.setSessionId(answer.getSessionId());
                dto.setImprovement(answer.getImprovement());

                if (answer.getEnrollment() != null) {
                        dto.setEnrollmentId(answer.getEnrollment().getId());
                }

                return dto;
        }

        private String normalizeAnswer(String answer) {
                return answer.trim()
                                .toLowerCase()
                                .replaceAll("\\s+", " ")
                                .replaceAll("[.!?]+$", "");
        }
}