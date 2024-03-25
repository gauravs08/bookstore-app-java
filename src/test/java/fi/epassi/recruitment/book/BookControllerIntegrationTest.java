package fi.epassi.recruitment.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.dto.BookDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@Rollback
@ComponentScan(basePackages = "fi.epassi.recruitment")
public class BookControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateBookEndpoint() throws Exception {
        // Given a BookDto object for the request body
        BookDto bookDto = BookDto.builder()
                .isbn(UUID.randomUUID())
                .author("J.R.R Tolkien")
                .title("The Return of the King")
                .price(BigDecimal.TEN)
                .build();

        // When making the POST request to create the book
        webTestClient.post()
                .uri("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
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
}
