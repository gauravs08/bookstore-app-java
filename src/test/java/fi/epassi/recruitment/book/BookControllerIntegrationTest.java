package fi.epassi.recruitment.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.exception.BookNotFoundException;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.services.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Rollback
@ExtendWith(SpringExtension.class)
//@ComponentScan(basePackages = "fi.epassi.recruitment")
public class BookControllerIntegrationTest {

    private static final BookModel BOOK_HOBBIT = BookModel.builder()
            .isbn(UUID.fromString("66737096-39ef-4a7c-aa4a-9fd018c14178"))
            .title("The Hobbit")
            .author("J.R.R Tolkien")
            .price(TEN)
            .build();
    private static final BookModel BOOK_FELLOWSHIP = BookModel.builder()
            .isbn(UUID.fromString("556aa37d-ef9c-45d3-ba4a-a792c123208a"))
            .title("The Fellowship of the Rings")
            .author("J.R.R Tolkien")
            .price(TEN)
            .build();
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;

    @Test
    void testGetBooks() {

        webTestClient.get()
                .uri("/api/v1/books")
                .exchange()
                .expectStatus().isOk()
                //.expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200)
                .jsonPath("$.response").isArray();
    }

    @Test
    void testCreateBookEndpoint() {
        when(bookService.createBook(any())).thenReturn(Mono.just(UUID.randomUUID()));

        // Given a BookDto object for the request body
        BookDto bookDto = BookDto.builder()
                .isbn(UUID.randomUUID())
                .author("J.R.R Tolkien")
                .title("The Return of the King")
                .price(BigDecimal.TEN)
                .build();

        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(bookDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .consumeWith(response -> {
                    // Parse the JSON response to ApiResponse object
                    ApiResponse apiResponse = objectMapper.convertValue(response.getResponseBody(), ApiResponse.class);

                    // Verify ApiResponse fields
                    assertThat(apiResponse.getStatusCode()).isEqualTo(200);
                    assertThat(apiResponse.getStatusMessage()).isEqualTo("OK");
                    assertThat(apiResponse.getResponse()).isNotNull();
                });
    }

    @Test
    void testUpdateBook() {
        when(bookService.updateBook(any())).thenReturn(Mono.just(UUID.randomUUID()));
        BookDto bookDto = new BookDto(UUID.randomUUID(), "Updated Title", "Updated Author", new BigDecimal("29.99"));
        webTestClient.put()
                .uri("/api/v1/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(bookDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200);
    }

    @Test
    void testGetBookByIsbn() {
        UUID isbn = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        webTestClient.get()
                .uri("/api/v1/books/{isbn}", isbn)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status_code").isEqualTo(200);
    }

    @Test
    void testDeleteBookByIsbn() {
        when(bookService.deleteBookWithIsbn(any())).thenReturn(Mono.empty());

        UUID isbn = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        webTestClient.delete()
                .uri("/api/v1/books/{isbn}", isbn)
                .exchange()
                .expectStatus().isOk();

        verify(bookService, times(1)).deleteBookWithIsbn(eq(isbn));

    }

    @Test
    void testDeleteBookByIsbn_BookNotFoundException() {
        // Mock the behavior of the service layer method to throw BookNotFoundException
        when(bookService.deleteBookWithIsbn(any())).thenReturn(Mono.error(new BookNotFoundException("ISBN", "123456")));

        UUID isbn = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        webTestClient.delete()
                .uri("/api/v1/books/{isbn}", isbn)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.title").isEqualTo("Not Found")
                .jsonPath("$.detail").isEqualTo("No book found with ISBN: " + isbn);
    }
}
