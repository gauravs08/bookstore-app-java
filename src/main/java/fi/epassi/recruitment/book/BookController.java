package fi.epassi.recruitment.book;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import fi.epassi.recruitment.api.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/books", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class BookController {

    private final BookService bookService;

    @GetMapping
    ApiResponse<List<BookDto>> getBooks(
        @RequestParam(value = "author", required = false) String author,
        @RequestParam(value = "title", required = false) String title) {
        return ApiResponse.ok(bookService.getBooks(author, title));
    }

    @PostMapping
    ApiResponse<UUID> createBook(@RequestBody @Validated BookDto bookDto) {
        var isbn = bookService.createBook(bookDto);
        return ApiResponse.ok(isbn);
    }

    @PutMapping
    ApiResponse<UUID> updateBook(@RequestBody @Validated BookDto bookDto) {
        var ret = bookService.updateBook(bookDto);
        return ApiResponse.ok(ret);
    }

    @GetMapping("/{isbn}")
    ApiResponse<BookDto> getBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        return ApiResponse.ok(bookService.getBookByIsbn(isbn));
    }

    @DeleteMapping("/{isbn}")
    ApiResponse<Void> deleteBookByIsbn(@PathVariable("isbn") @Validated UUID isbn) {
        bookService.deleteBookWithIsbn(isbn);
        return ApiResponse.ok();
    }

}