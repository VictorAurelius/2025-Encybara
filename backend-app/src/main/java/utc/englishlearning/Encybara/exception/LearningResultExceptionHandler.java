package utc.englishlearning.Encybara.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import utc.englishlearning.Encybara.domain.response.ApiErrorResponse;

@ControllerAdvice
public class LearningResultExceptionHandler {

        @ExceptionHandler(LearningResultException.LearningResultNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleLearningResultNotFound(
                        LearningResultException.LearningResultNotFoundException ex) {
                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Learning Result Not Found",
                                ex.getMessage());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(LearningResultException.InvalidSkillScoreException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidSkillScore(
                        LearningResultException.InvalidSkillScoreException ex) {
                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Skill Score",
                                ex.getMessage());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(LearningResultException.InvalidSkillTypeException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidSkillType(
                        LearningResultException.InvalidSkillTypeException ex) {
                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid Skill Type",
                                ex.getMessage());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(LearningResultException.InsufficientDataException.class)
        public ResponseEntity<ApiErrorResponse> handleInsufficientData(
                        LearningResultException.InsufficientDataException ex) {
                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Insufficient Data",
                                ex.getMessage());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(LearningResultException.class)
        public ResponseEntity<ApiErrorResponse> handleGenericLearningResultException(
                        LearningResultException ex) {
                ApiErrorResponse error = new ApiErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Learning Result Error",
                                ex.getMessage());
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}