package fi.epassi.recruitment.controller;

import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.api.ApiResponsePage;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponsePage<BookDto>> getBooks(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookService.getBooks(author, title, PageRequest.of(page, size, Sort.unsorted()));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<UUID>> createBook(@RequestBody @Validated BookDto bookDto) {
        return bookService.createBook(bookDto)
                .map(ApiResponse::ok);
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<UUID>> updateBook(@RequestBody @Validated BookDto bookDto) {
        return bookService.updateBook(bookDto)
                .map(ApiResponse::ok);
    }

    @GetMapping(value = "/{isbn}", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<BookDto>> getBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        return bookService.getBookByIsbn(isbn)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new BookNotFoundException("ISBN", isbn.toString())));

    }

    @DeleteMapping(value = "/{isbn}")
    ApiResponse<Void> deleteBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        try {
            bookService.deleteBookWithIsbn(isbn);
            return ApiResponse.ok();
        } catch (BookNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found", ex);
        }
    }

}