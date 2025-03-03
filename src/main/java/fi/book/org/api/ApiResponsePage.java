package fi.book.org.api;

import java.util.List;

import lombok.Getter;

import static org.springframework.http.HttpStatus.OK;

@Getter
public class ApiResponsePage<T> extends ApiResponse<List<T>> {


    private final int pageSize;
    private final int totalPages;
    private final int currentPage;
    private final long totalElements;

    public ApiResponsePage(int statusCode, String statusMessage, List<T> response, long totalElements, int totalPages, int currentPage, int pageSize) {
        super(statusCode, statusMessage, response);
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }


    public static <T> ApiResponsePage<T> okWithPagination(List<T> response, long totalElements, int totalPages, int currentPage, int pageSize) {
        return new ApiResponsePage<>(
                OK.value(),
                OK.getReasonPhrase(),
                response,
                totalElements,
                totalPages,
                currentPage,
                pageSize
        );
    }

}