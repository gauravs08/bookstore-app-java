package fi.epassi.recruitment.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import fi.epassi.recruitment.api.ApiResponse;

import java.util.UUID;

import fi.epassi.recruitment.api.ApiResponsePage;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/books")//, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class BookController {

    private final BookService bookService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    Flux<ApiResponsePage<BookDto>> getBooks(
        @RequestParam(value = "author", required = false) String author,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        Flux<BookDto> bookFlux =  bookService.getBooks(author, title, PageRequest.of(page, size, Sort.unsorted()));
        //long totalElements = bookflux.count().block();
        //int totalPages = ... // Calculate total pages from the flux and size
        //return ApiResponsePage.okWithPagination(bookflux, totalElements, totalPages, page, size);

        return bookFlux.count()
                .map(totalElements -> {
                    int totalPages = (int) Math.ceil((double) totalElements / size);
                    return ApiResponsePage.okWithPagination(bookFlux, totalElements, totalPages, page, size);
                })
                .flux()
                .switchIfEmpty(Mono.error(new BookNotFoundException()));

    }
//    @GetMapping
//    ResponseEntity<Object> getBooks(
//            @RequestParam(value = "author", required = false) String author,
//            @RequestParam(value = "title", required = false) String title,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        Page<BookDto> bookPage =  bookService.getBooks(author, title, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
//        Map<String, Object> response = new HashMap<>();
//        response.put("content", bookPage.getContent());
//        response.put("pageNumber", bookPage.getNumber());
//        response.put("pageSize", bookPage.getSize());
//        response.put("totalPages", bookPage.getTotalPages());
//        response.put("totalElements", bookPage.getTotalElements());
//
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//
//    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    ApiResponse<UUID> createBook(@RequestBody @Validated BookDto bookDto) {
        var isbn = bookService.createBook(bookDto);
        return ApiResponse.ok(isbn);
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    ApiResponse<UUID> updateBook(@RequestBody @Validated BookDto bookDto) {
        var ret = bookService.updateBook(bookDto);
        return ApiResponse.ok(ret);
    }

    @GetMapping(value = "/{isbn}",produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<BookDto>> getBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        //return ApiResponse.ok(bookService.getBookByIsbn(isbn));
        return bookService.getBookByIsbn(isbn)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new BookNotFoundException(isbn.toString())));

    }
    @DeleteMapping(value = "/{isbn}")
    ApiResponse<Void> deleteBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        bookService.deleteBookWithIsbn(isbn);
        return ApiResponse.ok();
    }

}