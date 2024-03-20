package fi.epassi.recruitment.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.zalando.problem.spring.web.advice.validation.ConstraintViolationAdviceTrait;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RequiredArgsConstructor
@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler implements ConstraintViolationAdviceTrait {

    private final MethodArgumentNotValidExceptionHandler methodArgumentNotValidExceptionHandler;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ResponseEntity handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException exception, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request
    ) {
        return methodArgumentNotValidExceptionHandler.handleMethodArgumentNotValid(exception, (NativeWebRequest) request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationExceptions(
            DataIntegrityViolationException ex, WebRequest request
    ) {
        var body = createProblemDetail(ex, BAD_REQUEST, getRootCause(ex).getLocalizedMessage(), null, null, request);
        return handleExceptionInternal(ex, body, new HttpHeaders(), BAD_REQUEST, request);
    }
}
