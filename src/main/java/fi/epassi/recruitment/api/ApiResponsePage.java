package fi.epassi.recruitment.api;

import lombok.Getter;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Getter
public class ApiResponsePage<T> extends ApiResponse<List<T>> {


    private int pageSize;
    private int totalPages;
    private int currentPage;
    private long totalElements;

    public ApiResponsePage(int statusCode, String statusMessage, List<T> response, long totalElements, int totalPages, int currentPage, int pageSize) {
        super(statusCode, statusMessage, response);
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }


    public static <T> ApiResponsePage<T> okWithPagination(Flux<T> flux, long totalElements, int totalPages, int currentPage, int pageSize) {
        return new ApiResponsePage<>(
                OK.value(),
                OK.getReasonPhrase(),
                flux.collectList().block(), // Collect all elements into a list
                totalElements,
                totalPages,
                currentPage,
                pageSize
        );
    }

    public static <T> ApiResponsePage<T> error(int statusCode, String statusMessage) {
        return new ApiResponsePage<>(statusCode, statusMessage, null, 0, 0, 0, 0);
    }

}