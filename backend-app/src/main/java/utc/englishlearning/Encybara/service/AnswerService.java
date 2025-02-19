package utc.englishlearning.Encybara.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.Answer;
import utc.englishlearning.Encybara.domain.Answer_Text;
import utc.englishlearning.Encybara.domain.Question;
import utc.englishlearning.Encybara.domain.response.answer.ResAnswerDTO;
import utc.englishlearning.Encybara.domain.request.answer.ReqCreateAnswerDTO;
import utc.englishlearning.Encybara.exception.ResourceNotFoundException;
import utc.englishlearning.Encybara.repository.AnswerRepository;
import utc.englishlearning.Encybara.repository.QuestionRepository;
import utc.englishlearning.Encybara.repository.AnswerTextRepository;
import utc.englishlearning.Encybara.domain.Question_Choice;
import utc.englishlearning.Encybara.repository.QuestionChoiceRepository;
import utc.englishlearning.Encybara.util.SecurityUtil;
import utc.englishlearning.Encybara.util.constant.QuestionTypeEnum;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.repository.UserRepository;

import java.util.List;
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

        public ResAnswerDTO createAnswer(ReqCreateAnswerDTO reqCreateAnswerDTO) {
                Question question = questionRepository.findById(reqCreateAnswerDTO.getQuestionId())
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

                User user = userRepository.findByEmail(SecurityUtil.getCurrentUserLogin()
                                .orElseThrow(() -> new RuntimeException("User not authenticated")));

                // Tìm các câu trả lời trước đó của người dùng cho câu hỏi này
                List<Answer> previousAnswers = answerRepository.findByUserAndQuestion(user, question);

                // Tính sessionId mới
                long newSessionId = previousAnswers.size() + 1;

                Answer answer = new Answer();
                answer.setQuestion(question);
                answer.setUser(user);
                answer.setPoint_achieved(0);
                answer.setSessionId(newSessionId); // Thiết lập sessionId tự động

                Answer_Text answerText = new Answer_Text();
                answerText.setAnsContent(reqCreateAnswerDTO.getAnswerContent());
                answerText.setAnswer(answer);
                answerTextRepository.save(answerText);

                answer = answerRepository.save(answer);
                return convertToDTO(answer, answerText);
        }

        public ResAnswerDTO createAnswerWithUserId(ReqCreateAnswerDTO reqCreateAnswerDTO, Long userId) {
                Question question = questionRepository.findById(reqCreateAnswerDTO.getQuestionId())
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Tìm các câu trả lời trước đó của người dùng cho câu hỏi này
                List<Answer> previousAnswers = answerRepository.findByUserAndQuestion(user, question);

                // Tính sessionId mới
                long newSessionId = previousAnswers.size() + 1;

                Answer answer = new Answer();
                answer.setQuestion(question);
                answer.setUser(user);
                answer.setPoint_achieved(0);
                answer.setSessionId(newSessionId); // Thiết lập sessionId tự động
                answer = answerRepository.save(answer);

                Answer_Text answerText = new Answer_Text();
                answerText.setAnsContent(reqCreateAnswerDTO.getAnswerContent());
                answerText.setAnswer(answer);
                answerTextRepository.save(answerText);

                return convertToDTO(answer, answerText);
        }

        public ResAnswerDTO getAnswerById(Long id) {
                Answer answer = answerRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
                Answer_Text answerText = answerTextRepository.findByAnswer(answer)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer text not found"));
                return convertToDTO(answer, answerText);
        }

        public List<Answer> getAnswersByQuestionId(Long questionId) {
                return answerRepository.findAll().stream()
                                .filter(answer -> Long.valueOf(answer.getQuestion().getId()).equals(questionId))
                                .collect(Collectors.toList());
        }

        public Page<Answer> getAllAnswersByQuestionIdAndUserId(Long questionId, Long userId, Pageable pageable) {
                List<Answer> allAnswers = answerRepository.findAll();

                // Filter answers by questionId and userId
                List<Answer> filteredAnswers = allAnswers.stream()
                                .filter(answer -> answer.getQuestion().getId() == questionId
                                                && answer.getUser().getId() == userId)
                                .collect(Collectors.toList());

                // Create a Page object
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), filteredAnswers.size());
                return new PageImpl<>(filteredAnswers.subList(start, end), pageable, filteredAnswers.size());
        }

        public void gradeAnswer(Long answerId) {
                Answer answer = answerRepository.findById(answerId)
                                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

                // Get the associated question
                Question question = answer.getQuestion();

                // Kiểm tra loại câu hỏi
                if (question.getQuesType() == QuestionTypeEnum.MULTIPLE) {
                        // Lấy các lựa chọn đúng cho câu hỏi
                        List<Question_Choice> choices = questionChoiceRepository.findByQuestionId(question.getId());
                        // Nối chuỗi các lựa chọn đúng
                        String correctAnswers = choices.stream()
                                        .filter(Question_Choice::isChoiceKey)
                                        .map(Question_Choice::getChoiceContent)
                                        .collect(Collectors.joining(", "));

                        // So sánh với nội dung câu trả lời
                        boolean isCorrect = correctAnswers.equals(answer.getAnswerText().getAnsContent());
                        answer.setPoint_achieved(isCorrect ? question.getPoint() : 0);
                } else {
                        // Get the correct choices for the question
                        List<Question_Choice> choices = questionChoiceRepository.findByQuestionId(question.getId());

                        // Check if the answer content matches any correct choice
                        boolean isCorrect = choices.stream()
                                        .anyMatch(choice -> choice.getChoiceContent()
                                                        .equals(answer.getAnswerText().getAnsContent())
                                                        && choice.isChoiceKey());

                        // Update point_achieved based on the result
                        answer.setPoint_achieved(isCorrect ? question.getPoint() : 0);
                }

                answerRepository.save(answer); // Save the updated answer
        }

        public Page<Answer> getAnswersByQuestionId(Long questionId, Pageable pageable) {
                List<Answer> allAnswers = answerRepository.findAll().stream()
                                .filter(answer -> Long.valueOf(answer.getQuestion().getId()).equals(questionId))
                                .collect(Collectors.toList());

                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allAnswers.size());
                return new PageImpl<>(allAnswers.subList(start, end), pageable, allAnswers.size());
        }

        public ResAnswerDTO getLatestAnswerByUserAndQuestion(Long questionId, Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Question question = questionRepository.findById(questionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

                // Lấy tất cả các câu trả lời của người dùng cho câu hỏi này
                List<Answer> userAnswers = answerRepository.findByUserAndQuestion(user, question);

                // Lấy câu trả lời mới nhất
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
                return dto;
        }
}