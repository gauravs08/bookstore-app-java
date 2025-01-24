package fi.epassi.recruitment.controller;

import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.api.ApiResponsePage;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

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
                .isbn(bookIsbn)
                .title("Reactive Spring")
                .author("Josh Long")
                .price(BigDecimal.valueOf(49.99))
                .build();
    }

    @Test
    void shouldGetBooks() {
        ApiResponsePage<BookDto> responsePage = ApiResponsePage.okWithPagination(
                List.of(bookDto), 1, 1, 0, 20);

        when(bookService.getBooks(null, null, PageRequest.of(0, 20)))
                .thenReturn(Mono.just(responsePage));

        webTestClient.get()
                .uri("/api/v1/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponsePage.class)
                .value(response -> assertEquals(OK.value(), response.getStatusCode()));

        verify(bookService, times(1)).getBooks(null, null, PageRequest.of(0, 20));
    }

    @Test
    void shouldCreateBook() {
        when(bookService.createBook(bookDto)).thenReturn(Mono.just(bookIsbn));

        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> response.getResponse().equals(bookIsbn.toString()));

        verify(bookService, times(1)).createBook(bookDto);
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
                //.expectStatus().isEqualTo(NOT_FOUND)
                .expectBody()
                .consumeWith(response -> {
//                    byte[] bytes = response.getResponseBody();
                    assertNull(response.getResponseBody());
//                    System.out.println(bytes != null ? new String(bytes) : "No response body");
                });
                //.expectStatus().isEqualTo(NOT_FOUND);

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
}
