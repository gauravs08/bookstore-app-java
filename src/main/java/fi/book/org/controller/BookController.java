package fi.book.org.controller;

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

import java.math.BigDecimal;
import java.util.UUID;

import fi.book.org.api.ApiResponse;
import fi.book.org.api.ApiResponsePage;
import fi.book.org.dto.BookDto;
import fi.book.org.exception.BookCreateException;
import fi.book.org.exception.BookNotFoundException;
import fi.book.org.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<ApiResponsePage<BookDto>> getBooks(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "bookstoreId", required = false) Long bookstoreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookService.getBooks(author, title, bookstoreId, PageRequest.of(page, size, Sort.unsorted()));
    }

    @PostMapping
    public Mono<ApiResponse<UUID>> createBook(
            @RequestParam UUID id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam BigDecimal price,
            @RequestParam("bookstore_id") Long bookstoreId) {
        BookDto bookDto = new BookDto(id, title, author, price, bookstoreId);
        return bookService.createBook(bookDto)
                .map(ApiResponse::ok)
                .onErrorResume(e -> {
                    log.error("Error creating book with ISBN {}: {}", bookDto.getId(), e.getMessage(), e);
                    return Mono.error(new BookCreateException("ISBN", bookDto.getId().toString())); // Pass the original exception as a cause
                });
    }


    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public Mono<ApiResponse<UUID>> updateBook(@RequestBody @Validated BookDto bookDto) {
        return bookService.updateBook(bookDto)
                .map(ApiResponse::ok)
                .onErrorResume(e -> {
                    log.error("Error updating book with ISBN {}: {}", bookDto.getId(), e.getMessage(), e);
                    return Mono.error(new BookCreateException("ISBN", bookDto.getId().toString()));
                });
    }

    @GetMapping(value = "/{isbn}", produces = APPLICATION_JSON_VALUE)
    public Mono<ApiResponse<BookDto>> getBookByIsbn(@PathVariable("isbn") @Validated UUID id) {
        return bookService.getBookByIsbn(id)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new BookNotFoundException("ISBN", id.toString())));

    }

    @DeleteMapping(value = "/{isbn}")
    public Mono<ApiResponse<Object>> deleteBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        return bookService.deleteBookWithIsbn(isbn)
                .thenReturn(ApiResponse.ok())
                .onErrorResume(BookNotFoundException.class, ex ->
                        Mono.error(new BookNotFoundException("ISBN", isbn.toString())));
    }

}