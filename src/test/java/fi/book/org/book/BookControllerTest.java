package fi.book.org.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import fi.book.org.api.ApiResponse;
import fi.book.org.api.ApiResponsePage;
import fi.book.org.controller.BookController;
import fi.book.org.dto.BookDto;
import fi.book.org.exception.BookNotFoundException;
import fi.book.org.services.BookService;
import fi.book.org.services.InventoryService;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private BookController bookController;

    private WebTestClient webTestClient;

    private BookDto bookDto;
    private UUID bookIsbn;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(bookController).build();

        bookIsbn = UUID.randomUUID();
        bookDto = BookDto.builder()
                .id(bookIsbn)
                .title("Reactive Spring")
                .author("Josh Long")
                .price(BigDecimal.valueOf(49.99))
                .bookstoreId(1001L)
                .build();
    }

    @Test
    void shouldGetBooks() {
        ApiResponsePage<BookDto> responsePage = ApiResponsePage.okWithPagination(
                List.of(bookDto), 1, 1, 0, 20);

        when(bookService.getBooks(null, null, null, PageRequest.of(0, 20)))
                .thenReturn(Mono.just(responsePage));

        webTestClient.get()
                .uri("/api/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponsePage.class)
                .value(response -> assertEquals(OK.value(), response.getStatusCode()));

        verify(bookService, times(1)).getBooks(null, null, null, PageRequest.of(0, 20));
    }

    @Test
    void shouldCreateBook() {
        when(bookService.createBook(bookDto)).thenReturn(Mono.just(bookIsbn));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/books")
                        .queryParam("id", bookIsbn)
                        .queryParam("title", "Reactive Spring")
                        .queryParam("author", "Josh Long")
                        .queryParam("price", 49.99)
                        .queryParam("bookstore_id", 1001L)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> response.getResponse().equals(bookIsbn.toString()));

        verify(bookService, times(1)).createBook(bookDto);
    }

    @Test
    void shouldHandleErrorWhenCreatingBook() {
        UUID id = UUID.randomUUID();
        String title = "Reactive Spring";
        String author = "Josh Long";
        BigDecimal price = BigDecimal.valueOf(49.99);
        Long bookstoreId = 1001L;

        when(bookService.createBook(any()))
                .thenReturn(Mono.error(new RuntimeException("Simulated service error")));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/books")
                        .queryParam("id", id.toString())
                        .queryParam("title", title)
                        .queryParam("author", author)
                        .queryParam("price", price.toString())
                        .queryParam("bookstore_id", bookstoreId)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class);
        verify(bookService, times(1)).createBook(any(BookDto.class));
    }


    @Test
    void shouldUpdateBook() {
        when(bookService.updateBook(bookDto)).thenReturn(Mono.just(bookIsbn));

        webTestClient.put()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> response.getResponse().equals(bookIsbn.toString()));

        verify(bookService, times(1)).updateBook(bookDto);
    }

    @Test
    void shouldHandleErrorWhenUpdatingBook() {
        when(bookService.updateBook(any()))
                .thenReturn(Mono.error(new RuntimeException("Simulated service error")));

        webTestClient.put()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class);
        verify(bookService, times(1)).updateBook(any(BookDto.class));
    }

    @Test
    void shouldGetBookByIsbn() {
        when(bookService.getBookByIsbn(bookIsbn)).thenReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/api/v1/books/{isbn}", bookIsbn)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> response.getResponse().equals(bookDto));

        verify(bookService, times(1)).getBookByIsbn(bookIsbn);
    }

    @Test
    void shouldReturnNotFoundForNonExistingBook() {
        when(bookService.getBookByIsbn(bookIsbn))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/v1/books/{isbn}", bookIsbn)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody()
                .consumeWith(response -> {
                    assertNull(response.getResponseBody());
                });

        verify(bookService, times(1)).getBookByIsbn(bookIsbn);
    }

    @Test
    void shouldDeleteBookByIsbn() {
        when(bookService.deleteBookWithIsbn(bookIsbn)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/books/{isbn}", bookIsbn)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> assertEquals(OK.value(), response.getStatusCode()));

        verify(bookService, times(1)).deleteBookWithIsbn(bookIsbn);
    }

    @Test
    void shouldHandleErrorWhenDeleteBook() {
        UUID isbn = UUID.randomUUID();
        BookNotFoundException exception = new BookNotFoundException("ISBN", isbn.toString());

        when(bookService.deleteBookWithIsbn(isbn)).thenReturn(Mono.error(exception));

        webTestClient.delete()
                .uri("/api/v1/books/{isbn}", isbn)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class);

        verify(bookService, times(1)).deleteBookWithIsbn(isbn);
    }
}
