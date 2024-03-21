package fi.epassi.recruitment.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.epassi.recruitment.book.BookDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private int statusCode;

    private String statusMessage;

    @JsonInclude(NON_NULL)
    private T response;
    // Pagination fields
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    public ApiResponse(HttpStatus status, String message, T response,
                       int pageNumber, int pageSize, int totalPages, long totalElements) {
        this(status.value(), message, response, pageNumber, pageSize, totalPages, totalElements);
    }

    public ApiResponse(HttpStatus status, String message, T response) {
        this(status, message, response, 0, 0, 0, 0);
    }

    public static <T> ApiResponse<T> okWithPagination(Page<T> page) {
        return (ApiResponse<T>) new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.getReasonPhrase(),
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
    public static <T> ApiResponse<T> ok() {
        return buildResponse(OK, null);
    }

    public static <T> ApiResponse<T> ok(T responseObject) {
        return buildResponse(OK, responseObject);
    }

    public static <T> ApiResponse<T> ok(String statusMessage, T responseObject) {
        return buildResponse(OK, statusMessage, responseObject);
    }

    public static <T> ApiResponse<T> buildResponse(HttpStatus status, T responseObject) {
        return new ApiResponse<>(status.value(), status.getReasonPhrase(), responseObject,0,0,0,0);
    }

    public static <T> ApiResponse<T> buildResponse(HttpStatus status, String message, T responseObject) {
        return new ApiResponse<>(status.value(), message, responseObject,0,0,0,0);
    }
}