package fi.epassi.recruitment.book;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.epassi.recruitment.BaseIntegrationTest;
import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.dto.BookDto;
import fi.epassi.recruitment.model.BookModel;
import fi.epassi.recruitment.repository.BookRepository;
import fi.epassi.recruitment.repository.InventoryRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static java.math.BigDecimal.TEN;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTest extends BaseIntegrationTest {

    private static final String BASE_PATH_V1_BOOK = "/api/v1/books";
    private static final String AUTHOR = "author";
    private static final String TITLE = "title";
    private static final String BASE_PATH_V1_BOOK_BY_ISBN = BASE_PATH_V1_BOOK + "/{isbn}";

    @Autowired
    private ReactiveTransactionManager reactiveTransactionManager;
    private static final BookModel BOOK_MODEL_HOBBIT = BookModel.builder()
            .isbn(UUID.fromString("66737096-39ef-4a7c-aa4a-9fd018c14178"))
            .title("The Hobbit")
            .author("J.R.R Tolkien")
            .price(TEN)
            .newBook(false)
            .build();

    private static final BookModel BOOK_MODEL_FELLOWSHIP = BookModel.builder()
            .isbn(UUID.fromString("556aa37d-ef9c-45d3-ba4a-a792c123208a"))
            .title("The Fellowship of the Rings")
            .author("J.R.R Tolkien")
            .price(TEN).newBook(false)
            .build();

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

//    @Test
//    void testCreateBookEndpoint() throws Exception {
//        // Given a BookDto object for the request body
//        BookDto bookDto = BookDto.builder()
//                .isbn(UUID.randomUUID())
//                .author("J.R.R Tolkien")
//                .title("The Return of the King")
//                .price(BigDecimal.TEN)
//                .build();
//        // When making the POST request to create the book
//        String responseJson = mvc.perform(post("/api/v1/books")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(bookDto)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//        System.out.println("Response Body: " + responseJson);
//        // Parse the JSON response to ApiResponse object
//        ApiResponse<UUID> apiResponse = mapper.readValue(responseJson, new TypeReference<ApiResponse<UUID>>() {});
//
//        // Verify ApiResponse fields
//        assertThat(apiResponse.getStatusCode()).isEqualTo(200); // Assuming OK status
//        assertThat(apiResponse.getStatusMessage()).isEqualTo("OK");
//        assertThat(apiResponse.getResponse()).isEqualTo(bookDto.getIsbn());
//    }

    // Utility method to convert object to JSON string
    private String asJsonString(Object obj) throws Exception {
        var ret = new ObjectMapper().writeValueAsString(obj);
        System.out.println("responseBody:"+ret);
        return ret;
    }
    @Test
    //@SneakyThrows
    void shouldCreateBookAndReturnId() throws JsonProcessingException {
        UUID EXAMPLE_UUID = UUID.randomUUID();

        // Given
        var bookDto = BookDto.builder().isbn(EXAMPLE_UUID).title("The Two Towers").author("J.R.R Tolkien").price(TEN).build();
        var bookDtoJson = mapper.writeValueAsString(bookDto);

        // When
        //var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK);
        //var request = post(requestUrl).contentType(APPLICATION_JSON).content(bookDtoJson);
        //var response = mvc.perform(request);
//        TransactionalOperator.create(reactiveTransactionManager)
//                .execute(status -> {
//                    try {
//                        mvc.perform(post(getEndpointUrl(BASE_PATH_V1_BOOK))
//                                        .contentType(APPLICATION_JSON)
//                                        .content(bookDtoJson))
//                                .andExpect(status().is2xxSuccessful())
//                                //.andExpect(jsonPath("$.response", is(notNullValue())))
//                                //.andExpect(jsonPath("$.title", is("Not Found")))
//                                .andReturn();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                    return null;
//                }).then()
//                .as(StepVerifier::create)
//                .expectComplete()
//                .verify();
        // When
        // When making the POST request to create the book
        ObjectMapper objectMapper = new ObjectMapper();
        Mono<ApiResponse<UUID>> responseMono = Mono.fromCallable(() ->
                mvc.perform(post(getEndpointUrl(BASE_PATH_V1_BOOK))
                                .contentType(APPLICATION_JSON)
                                .content(bookDtoJson))

                        .andExpect(status().isOk()) // Assuming successful creation returns OK status
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).map(responseBody -> {
            try {
                return objectMapper.readValue(responseBody, new TypeReference<ApiResponse<UUID>>(){});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        // Then print and verify the response
        responseMono.subscribe(response -> {
            System.out.println("Response Body: " + response);
            // Add your assertions here if needed
        });


    // Then
//        response.andExpect(status().is2xxSuccessful())
//            .andExpect(jsonPath("$.response", is(notNullValue())));

}
    @Test
    @SneakyThrows
    void shouldRespondWithAllBooks() {
        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK);
        var request = get(requestUrl).contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.response", is(notNullValue())));
    }

    @Test
    @SneakyThrows
    void shouldRespondWithBookWhenSearchingByAuthor() {
        // Given
        bookRepository.save(BOOK_MODEL_HOBBIT);
        bookRepository.save(BOOK_MODEL_FELLOWSHIP);

        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK);
        var request = get(requestUrl).queryParam(AUTHOR, "J.R.R Tolkien").contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.response[0].author", is("J.R.R Tolkien")))
            .andExpect(jsonPath("$.response[0].title", is(notNullValue())));
    }

    @Test
    @SneakyThrows
    void shouldRespondWithBooksWhenSearchingByTitle() {
        // Given
        bookRepository.save(BOOK_MODEL_HOBBIT);

        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK);
        var request = get(requestUrl).queryParam(TITLE, "The Hobbit").contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.response[0].author", is("J.R.R Tolkien")))
            .andExpect(jsonPath("$.response[0].title", is("The Hobbit")));
    }

    @Test
    @SneakyThrows
    void shouldRespondWithEmptyResponseWhenSearchingForNonExistingBooksByAuthor() {
        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK);
        var request = get(requestUrl).queryParam(AUTHOR, "Stephen King").contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.response", is(empty())));
    }

    @Test
    @SneakyThrows
    void shouldRespondWithFoundWhenSearchingForNonExistingBookByIsbn() {
        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK_BY_ISBN);

        var request = get(requestUrl, UUID.randomUUID()).contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.status", is(NOT_FOUND.value())))
            .andExpect(jsonPath("$.title", is("Not Found")));
    }

    @Test
    @SneakyThrows
    void shouldDeleteBookByIsbnSuccessfully() {
        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK_BY_ISBN);

        var request = delete(requestUrl, UUID.randomUUID()).contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is2xxSuccessful());
    }

    @Test
    @SneakyThrows
    void shouldRespondWithBadRequestWhenDeletingWhereIsbnIsNotUUID() {
        // When
        var requestUrl = getEndpointUrl(BASE_PATH_V1_BOOK_BY_ISBN);
        var request = delete(requestUrl, "blaha").contentType(APPLICATION_JSON);
        var response = mvc.perform(request);

        // Then
        response.andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    void shouldRespondWithBadRequestWhenCreatingBookWithNoTitle() {
        // Given
        var bookDto = BookDto.builder().isbn(UUID.randomUUID()).author("J.R.R Tolkien").price(TEN).build();
        var bookDtoJson = mapper.writeValueAsString(bookDto);

        // When
        var response = mvc.perform(post(getEndpointUrl(BASE_PATH_V1_BOOK)).contentType(APPLICATION_JSON).content(bookDtoJson));

        // Then bad request since title is required.
        response.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.status", is(BAD_REQUEST.name())))
            .andExpect(jsonPath("$.violations[0].field", is("title")))
            .andExpect(jsonPath("$.violations[0].message", is("must not be blank")));
    }

    @Test
    @SneakyThrows
    void shouldUpdateExistingBookSuccessfully() {
        // Given
        var saved = bookRepository.save(BOOK_MODEL_FELLOWSHIP).block();

        // When
        var bookDto = BookDto.builder().isbn(saved.getIsbn())
            .author("J.R.R Tolkien")
            .title("The Return of the King")
            .price(TEN)
            .build();
        var bookDtoJson = mapper.writeValueAsString(bookDto);

        var response = mvc.perform(put(getEndpointUrl(BASE_PATH_V1_BOOK)).contentType(APPLICATION_JSON).content(bookDtoJson));

        var responseBody = response.andReturn().getResponse().getContentAsString();
        System.out.println("responseBody------"+responseBody);
        // Then
        response.andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.status_code", is(OK.value())));
    }

    @Test
    @SneakyThrows
    void shouldRespondWithNotFoundWhenUpdatingNonExistingBook() {
        // Given a random isbn that should not exist should result in a HTTP404
        var bookDto = BookDto.builder().isbn(UUID.randomUUID())
            .author("J.R.R Tolkien")
            .title("The Return of the King")
            .price(TEN)
            .build();
        var bookDtoJson = mapper.writeValueAsString(bookDto);

        // When
        var response = mvc.perform(put(getEndpointUrl(BASE_PATH_V1_BOOK)).contentType(APPLICATION_JSON).content(bookDtoJson));

        // Then
        response.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.status", is(NOT_FOUND.value())))
            .andExpect(jsonPath("$.title", is("Not Found")));
    }

}
