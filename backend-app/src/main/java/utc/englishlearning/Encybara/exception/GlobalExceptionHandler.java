package utc.englishlearning.Encybara.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import utc.englishlearning.Encybara.domain.response.ApiErrorResponse;
import utc.englishlearning.Encybara.domain.response.RestResponse;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<RestResponse<Object>> handleAuthenticationException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Authentication error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Validation error");

        List<String> errors = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        res.setMessage(String.join(", ", errors));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(IdInvalidException.class)
    public ResponseEntity<String> handleIdInvalidException(IdInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(LearningMaterialNotFoundException.class)
    public ResponseEntity<RestResponse<String>> handleLearningMaterialNotFoundException(
            LearningMaterialNotFoundException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<RestResponse<String>> handleStorageException(StorageException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<String>> handleCustomExceptions(Exception ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<RestResponse<String>> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.CONFLICT.value());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<RestResponse<String>> handleInvalidOperationException(InvalidOperationException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DictionaryException.class)
    public ResponseEntity<RestResponse<String>> handleDictionaryException(DictionaryException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleFileStorageException(FileStorageException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "File upload error",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "File too large",
                "Uploaded file exceeds the maximum allowed size");
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(NoSuitableCoursesException.class)
    public ResponseEntity<RestResponse<String>> handleNoSuitableCoursesException(NoSuitableCoursesException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RestResponse<String>> handleIllegalStateException(IllegalStateException ex) {
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Failed to process learning result: " + ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}