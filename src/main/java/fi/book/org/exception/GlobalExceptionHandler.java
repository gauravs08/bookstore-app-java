package fi.book.org.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fi.book.org.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleApplicationException(ApplicationException ex) {
        log.error("Application error: {}", ex.getMessage());
        int status = ex.getStatusCode().value();
        String detail = ex.getBody().getDetail() != null ? ex.getBody().getDetail() : ex.getMessage();
        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.error(status, detail)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(500, "An unexpected error occurred")));
    }
}
